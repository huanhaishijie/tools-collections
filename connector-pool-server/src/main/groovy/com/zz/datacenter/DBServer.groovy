package com.zz.datacenter

import com.google.protobuf.ByteString
import com.yuezm.project.connector.proto.DataSourceInfo
import com.yuezm.project.connector.proto.Response
import com.yuezm.project.connector.proto.*
import com.zaxxer.hikari.HikariDataSource
import groovy.json.JsonGenerator
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilerConfiguration

import javax.sql.DataSource
import java.security.MessageDigest
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.concurrent.ConcurrentHashMap

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
            new ConcurrentHashMap<String, HikariDataSource>()

    /* ======================== util ======================== */

    private static String calcKey(DataSourceInfo info) {
        String link = info.url + ":" + info.username + ":" + info.password
        MessageDigest md = MessageDigest.getInstance("MD5")
        byte[] digest = md.digest(link.getBytes("UTF-8"))
        return digest.encodeHex().toString()
    }

    private static String getOther(DataSourceInfo info, String key) {
        def v = info.otherMap?.get(key)
        return v == null ? "" : v.toString()
    }

    private static Object parseParams(String paramsStr) {
        if (!paramsStr) return null
        try {
            return new JsonSlurper().parseText(paramsStr)
        } catch (Exception e) {
            return paramsStr
        }
    }

    protected static void shutdown() {
        DATA_SOURCES.values().each {
            try { it.close() } catch (Exception ignored) {}
        }
    }

    /* ======================== RowSet ======================== */

    private static RowSet buildRowSetFast(ResultSet rs) {
        def meta = rs.metaData
        final int colCount = meta.columnCount

        final int[] jdbcTypes = new int[colCount]
        for (int i = 1; i <= colCount; i++) {
            jdbcTypes[i - 1] = meta.getColumnType(i)
        }

        RowSet.Builder rsBuilder = RowSet.newBuilder()

        for (int i = 1; i <= colCount; i++) {
            rsBuilder.addColumns(
                    ColumnMeta.newBuilder()
                            .setName(meta.getColumnLabel(i))
                            .setJdbcType(jdbcTypes[i - 1])
                            .setTypeName(meta.getColumnTypeName(i))
            )
        }

        while (rs.next()) {
            Row.Builder row = Row.newBuilder()

            for (int i = 0; i < colCount; i++) {
                int t = jdbcTypes[i]
                Value.Builder vb = Value.newBuilder()

                switch (t) {
                    case java.sql.Types.INTEGER:
                    case java.sql.Types.SMALLINT:
                    case java.sql.Types.TINYINT:
                        int iv = rs.getInt(i + 1)
                        if (rs.wasNull()) vb.setIsNull(true)
                        else vb.setIntVal(iv)
                        break

                    case java.sql.Types.BIGINT:
                        long lv = rs.getLong(i + 1)
                        if (rs.wasNull()) vb.setIsNull(true)
                        else vb.setLongVal(lv)
                        break

                    case java.sql.Types.FLOAT:
                    case java.sql.Types.REAL:
                    case java.sql.Types.DOUBLE:
                        double dv = rs.getDouble(i + 1)
                        if (rs.wasNull()) vb.setIsNull(true)
                        else vb.setDoubleVal(dv)
                        break

                    case java.sql.Types.DECIMAL:
                    case java.sql.Types.NUMERIC:
                        BigDecimal bd = rs.getBigDecimal(i + 1)
                        if (bd == null) vb.setIsNull(true)
                        else vb.setStringVal(bd.toPlainString())
                        break

                    case java.sql.Types.TIMESTAMP:
                    case java.sql.Types.DATE:
                        java.sql.Timestamp ts = rs.getTimestamp(i + 1)
                        if (ts == null) vb.setIsNull(true)
                        else vb.setTimeVal(TimeVal.newBuilder().setEpochMillis(ts.time))
                        break

                    case java.sql.Types.BINARY:
                    case java.sql.Types.VARBINARY:
                    case java.sql.Types.BLOB:
                        byte[] b = rs.getBytes(i + 1)
                        if (b == null) vb.setIsNull(true)
                        else vb.setBytesVal(ByteString.copyFrom(b))
                        break

                    default:
                        String s = rs.getString(i + 1)
                        if (s == null) vb.setIsNull(true)
                        else vb.setStringVal(s)
                }

                row.addValues(vb)
            }

            rsBuilder.addRows(row)
        }

        return rsBuilder.build()
    }

    /* ======================== SQL ======================== */

    private static <T> T withSql(String key, Closure<T> action) {
        HikariDataSource ds = DATA_SOURCES.get(key)
        if (ds == null) {
            throw new IllegalStateException("no datasource for key=" + key)
        }

        Sql sql = null
        try {
            sql = new Sql(ds as DataSource)
            return action.call(sql)
        } finally {
            try { sql?.close() } catch (Exception ignored) {}
        }
    }

    /* ======================== API ======================== */

    Response registerDb(DataSourceInfo info) {
        String key = calcKey(info)

        if (DATA_SOURCES.containsKey(key)) {
            return Response.newBuilder()
                    .setCode(OK_CODE)
                    .setMessage(OK)
                    .putData("res", key)
                    .build()
        }

        HikariDataSource ds = new HikariDataSource()
        try {
            ds.setDriverClassName(info.type)
            ds.setJdbcUrl(info.url)
            ds.setUsername(info.username)
            ds.setPassword(info.password)

            if (info.maxPoolSize > 0) ds.setMaximumPoolSize(info.maxPoolSize)
            if (info.minPoolSize > 0) ds.setMinimumIdle(info.minPoolSize)
            if (info.idleTimeout > 0) ds.setIdleTimeout(info.idleTimeout)
            if (info.connectionTimeout > 0) ds.setConnectionTimeout(info.connectionTimeout)

            ds.setLeakDetectionThreshold(60000)
            ds.setValidationTimeout(5000)
            ds.setMaxLifetime(30 * 60 * 1000)

            info.otherMap?.each { k, v ->
                ds.addDataSourceProperty(k.toString(), v)
            }

            verifyDataSource(ds)
            DATA_SOURCES.put(key, ds)

            return Response.newBuilder()
                    .setCode(OK_CODE)
                    .setMessage(OK)
                    .putData("res", key)
                    .build()

        } catch (Exception e) {
            try { ds.close() } catch (Exception ignored) {}
            return Response.newBuilder()
                    .setCode(ERR_CODE)
                    .setMessage(ERR + ": " + e.message)
                    .build()
        }
    }

    private static void verifyDataSource(HikariDataSource ds) {
        Connection conn = null
        try {
            conn = ds.getConnection()
            if (!conn.isValid(3)) {
                throw new SQLException("Connection validation failed")
            }
        } finally {
            try { conn?.close() } catch (Exception ignored) {}
        }
    }

    Response execSql(DataSourceInfo info) {
        String key = getOther(info, "key")
        if (!key) key = calcKey(info)

        if (!DATA_SOURCES.containsKey(key)) {
            return Response.newBuilder()
                    .setCode(ERR_CODE)
                    .setMessage(ERR + ": no datasource")
                    .build()
        }

        String sqlStr = getOther(info, "sql")
        String exec = getOther(info, "exec")
        String execPlus = getOther(info, "execPlus")
        String execCode = getOther(info, "execCode")
        Object params = parseParams(getOther(info, "params"))

        try {
            if (execPlus) {
                return withSql(key) { Sql sql ->
                    execPlusInternal(sql, sqlStr, execCode, params)
                }
            }

            Object result = withSql(key) { Sql sql ->
                if ("rows".equals(exec)) {
                    Statement stmt = null
                    ResultSet rs = null
                    try {
                        stmt = sql.connection.createStatement()
                        stmt.fetchSize = 1000
                        rs = stmt.executeQuery(sqlStr)
                        return buildRowSetFast(rs)
                    } finally {
                        try { rs?.close() } catch (Exception ignored) {}
                        try { stmt?.close() } catch (Exception ignored) {}
                    }
                }
                if ("execute".equals(exec)) return sql.execute(sqlStr)
                if ("firstRow".equals(exec)) return sql.firstRow(sqlStr)
                return null
            }

            Response.Builder rb = Response.newBuilder()
                    .setCode(OK_CODE)
                    .setMessage(OK)

            if (result != null) {
                if ("rows".equals(exec)) {
                    rb.putData("res", "rowset")
                    rb.setRowset((result as RowSet).toByteString())
                } else {
                    rb.putData("res", JSON.toJson(result))
                }
            }
            return rb.build()

        } catch (Exception e) {
            return Response.newBuilder()
                    .setCode(ERR_CODE)
                    .setMessage(ERR + ": " + e.message)
                    .build()
        }
    }

    private static Response execPlusInternal(Sql sql, String sqlStr, String execCode, Object params) {
        try {
            Binding binding = new Binding()
            binding.setVariable("sqlHandler", sql)
            binding.setVariable("sqlStr", sqlStr)
            binding.setVariable("JSON", JSON)
            binding.setVariable("log", log)

            if (params instanceof Map) binding.variables.putAll(params as Map)
            else if (params instanceof List) binding.setVariable("args", params)
            else if (params != null) binding.setVariable("args", [params])

            CompilerConfiguration cc = new CompilerConfiguration()
            GroovyShell shell = new GroovyShell(DBServer.class.classLoader, binding, cc)
            Object result = shell.evaluate(execCode)

            Response.Builder rb = Response.newBuilder()
                    .setCode(OK_CODE)
                    .setMessage(OK)

            if (result != null) rb.putData("res", JSON.toJson(result))
            return rb.build()

        } catch (Exception e) {
            return Response.newBuilder()
                    .setCode(ERR_CODE)
                    .setMessage(ERR + ": " + e.message)
                    .build()
        }
    }

    Response handle(byte[] bytes) {
        try {
            DataSourceInfo info = DataSourceInfo.parseFrom(bytes)
            String method = info.hasExec() ? info.exec.method : ""

            switch (method) {
                case "registerDb": return registerDb(info)
                case "removeDb":   return removeDb(info)
                case "execSql":    return execSql(info)
                case "verifyDb":   return verifyDb(info)
                default:
                    return Response.newBuilder()
                            .setCode(UNSUPPORTED_METHOD_CODE)
                            .setMessage(UNSUPPORTED_METHOD)
                            .build()
            }
        } catch (Exception e) {
            return Response.newBuilder()
                    .setCode(INVALID_MESSAGE_CODE)
                    .setMessage(INVALID_MESSAGE + ": " + e.message)
                    .build()
        }
    }

    Response removeDb(DataSourceInfo info) {
        String key = getOther(info, "key")
        if (!key) key = calcKey(info)

        HikariDataSource ds = DATA_SOURCES.remove(key)
        try { ds?.close() } catch (Exception ignored) {}

        return Response.newBuilder()
                .setCode(OK_CODE)
                .setMessage(OK)
                .build()
    }

    Response verifyDb(DataSourceInfo info) {
        try {
            verifyDataSource(DATA_SOURCES.get(calcKey(info)))
            return Response.newBuilder()
                    .setCode(OK_CODE)
                    .setMessage(OK)
                    .putData("res", "true")
                    .build()
        } catch (Exception e) {
            return Response.newBuilder()
                    .setCode(ERR_CODE)
                    .setMessage(ERR + ": " + e.message)
                    .build()
        }
    }
}
