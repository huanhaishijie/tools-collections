package com.yuezm.project.connector

import com.yuezm.project.connector.proto.DataSourceInfo
import com.yuezm.project.connector.proto.Response
import com.zaxxer.hikari.HikariDataSource
import groovy.json.JsonGenerator
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.util.logging.Slf4j

import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.ConcurrentHashMap

/**
 * Server
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/12/12 10:53
 */

@Slf4j
class DBServer {

    /* ======================== 常量 ======================== */

    private static final String OK = "OK"
    private static final String ERR = "ERR"
    private static final String UNSUPPORTED_METHOD = "Unsupported method"
    private static final String INVALID_MESSAGE = "Invalid message"

    private static final int OK_CODE = 0
    private static final int ERR_CODE = -1
    private static final int UNSUPPORTED_METHOD_CODE = -2
    private static final int INVALID_MESSAGE_CODE = -3

    private static final JsonGenerator JSON =
            new JsonGenerator.Options().disableUnicodeEscaping().build()


    /** 只缓存 DataSource（线程安全） */
    private static final Map<String, HikariDataSource> DATA_SOURCES =
            new ConcurrentHashMap<>()


    private static String calcKey(DataSourceInfo info) {
        String link = "${info.getUrl()}:${info.getUsername()}:${info.getPassword()}"
        return link.bytes.md5()
    }

    protected static void  shutdown(){
        DATA_SOURCES.values().each { try {
            it.close()
        } catch (Exception e) {
            log.warn("close datasource error", e)
        } }
    }

    private static <T> T withSql(String key, Closure<T> action) {
        HikariDataSource ds = DATA_SOURCES[key]
        if (!ds) {
            throw new IllegalStateException("no datasource for key=$key")
        }

        Sql sql = null
        try {
            sql = new Sql(ds as DataSource)
            return action(sql)
        } finally {
            if (sql != null) {
                try {
                    sql.close()
                } catch (ignored) {}
            }
        }
    }

    private static Object parseParams(String paramsStr) {
        if (!paramsStr) return null
        try {
            return new JsonSlurper().parseText(paramsStr)
        } catch (Exception e) {
            return paramsStr
        }
    }


    Response registerDb(DataSourceInfo info) {
        String key = calcKey(info)
        HikariDataSource ds = null

        try {
            if (DATA_SOURCES.containsKey(key)) {
                return Response.newBuilder()
                        .setCode(OK_CODE)
                        .setMessage(OK)
                        .putData("res", key)
                        .build()
            }

            ds = new HikariDataSource()
            ds.setDriverClassName(info.getType())
            ds.setJdbcUrl(info.getUrl())
            ds.setUsername(info.getUsername())
            ds.setPassword(info.getPassword())

            if (info.getMaxPoolSize() > 0) ds.setMaximumPoolSize(info.getMaxPoolSize())
            if (info.getMinPoolSize() > 0) ds.setMinimumIdle(info.getMinPoolSize())
            if (info.getIdleTimeout() > 0) ds.setIdleTimeout(info.getIdleTimeout())
            if (info.getConnectionTimeout() > 0) ds.setConnectionTimeout(info.getConnectionTimeout())

            // 连接池稳定性配置
            ds.setLeakDetectionThreshold(60_000)
            ds.setValidationTimeout(5_000)
            ds.setMaxLifetime(30 * 60 * 1000)

            info.getOtherMap().forEach { k, v ->
                ds.addDataSourceProperty(k, v)
            }

            // ====== 关键：可用性校验 ======
            verifyDataSource(ds)

            DATA_SOURCES[key] = ds

            return Response.newBuilder()
                    .setCode(OK_CODE)
                    .setMessage(OK)
                    .putData("res", key)
                    .build()

        } catch (Exception e) {
            if (ds != null) {
                try {
                    ds.close()
                } catch (ignored) {}
            }

            return Response.newBuilder()
                    .setCode(ERR_CODE)
                    .setMessage("$ERR: ${e.message}")
                    .build()
        }
    }

    private static void verifyDataSource(HikariDataSource ds) {
        Connection conn = null
        try {
            conn = ds.getConnection()

            // JDBC 标准校验（推荐）
            if (!conn.isValid(3)) {
                throw new SQLException("Connection validation failed")
            }

        } finally {
            if (conn != null) {
                conn.close()
            }
        }
    }

    Response removeDb(DataSourceInfo info) {
        String key = info.getOtherOrDefault("key", "")
        if (!key) {
            key = calcKey(info)
        }

        HikariDataSource ds = DATA_SOURCES.remove(key)
        if (ds) {
            try {
                ds.close()
            } catch (Exception e) {
                log.warn("close datasource error", e)
            }
        }

        return Response.newBuilder()
                .setCode(OK_CODE)
                .setMessage(OK)
                .build()
    }

    Response execSql(DataSourceInfo info) {
        String key = info.getOtherOrDefault("key", "")
        if (!key) {
            key = calcKey(info)
        }

        if (!DATA_SOURCES.containsKey(key)) {
            return Response.newBuilder()
                    .setCode(ERR_CODE)
                    .setMessage("$ERR: no datasource")
                    .build()
        }

        String sqlStr   = info.getOtherOrDefault("sql", "")
        String exec     = info.getOtherOrDefault("exec", "")
        String execPlus = info.getOtherOrDefault("execPlus", "")
        String execCode = info.getOtherOrDefault("execCode", "")
        Object params   = parseParams(info.getOtherOrDefault("params", ""))

        try {
            // ===== execPlus：直接返回 Response =====
            if (execPlus) {
                return withSql(key) { Sql sql ->
                    execPlusInternal(sql, sqlStr, execCode, params)
                }
            }

            // ===== 普通 SQL 执行 =====
            Object result = withSql(key) { Sql sql ->
                if (params instanceof Map) {
                    switch (exec) {
                        case "rows":     return sql.rows(params, sqlStr)
                        case "execute":  return sql.execute(params, sqlStr)
                        case "firstRow": return sql.firstRow(params, sqlStr)
                    }
                } else if (params instanceof List) {
                    switch (exec) {
                        case "rows":     return sql.rows(sqlStr, params)
                        case "execute":  return sql.execute(sqlStr, params)
                        case "firstRow": return sql.firstRow(sqlStr, params)
                    }
                } else {
                    switch (exec) {
                        case "rows":     return sql.rows(sqlStr)
                        case "execute":  return sql.execute(sqlStr)
                        case "firstRow": return sql.firstRow(sqlStr)
                    }
                }
                return null
            }

            def builder = Response.newBuilder()
                    .setCode(OK_CODE)
                    .setMessage(OK)

            if (result != null) {
                builder.putData("res", JSON.toJson(result))
            }

            return builder.build()

        } catch (Exception e) {
            e.printStackTrace()
            return Response.newBuilder()
                    .setCode(ERR_CODE)
                    .setMessage("$ERR: ${e.message}")
                    .build()
        }
    }

    private static Response execPlusInternal(
            Sql sql, String sqlStr, String execCode, Object params) {

        try {
            Binding binding = new Binding()
            binding.setVariable("sqlHandler", sql)
            binding.setVariable("sqlStr", sqlStr)
            binding.setVariable("JSON", JSON)
            binding.setVariable("log", log)

            if (params instanceof Map) {
                binding.variables.putAll(params as Map)
            } else if (params instanceof List) {
                binding.setVariable("args", params)
            } else if (params != null) {
                binding.setVariable("args", [params])
            }

            def shell = new GroovyShell(binding)
            println "execCode: $execCode"
            def result = shell.evaluate(execCode)


            def builder = Response.newBuilder()
                    .setCode(OK_CODE)
                    .setMessage(OK)

            if (result != null) {
                builder.putData("res", JSON.toJson(result))
            }

            return builder.build()

        } catch (Exception e) {
            e.printStackTrace()
            log.error("execPlusInternal error", e)
            return Response.newBuilder()
                    .setCode(ERR_CODE)
                    .setMessage("$ERR: ${e.message}")
                    .build()
        }
    }

    Response handle(byte[] bytes) {
        try {
            DataSourceInfo info = DataSourceInfo.parseFrom(bytes)
            String method = info.hasExec() ? info.getExec().getMethod() : ""

            return switch (method) {
                case "registerDb" -> registerDb(info)
                case "removeDb" -> removeDb(info)
                case "execSql" -> execSql(info)
                default -> Response.newBuilder()
                        .setCode(UNSUPPORTED_METHOD_CODE)
                        .setMessage(UNSUPPORTED_METHOD)
                        .build()
            }

        } catch (Exception e) {
            return Response.newBuilder()
                    .setCode(INVALID_MESSAGE_CODE)
                    .setMessage("$INVALID_MESSAGE: ${e.message}")
                    .build()
        }
    }
}


