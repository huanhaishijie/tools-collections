package com.yuezm.project.connector

import com.yuezm.project.connector.proto.DataSourceInfo
import com.yuezm.project.connector.proto.Response
import com.zaxxer.hikari.HikariDataSource
import groovy.sql.Sql
import groovy.util.logging.Slf4j

import javax.sql.DataSource
import java.util.concurrent.ConcurrentHashMap
import groovy.json.JsonSlurper
import groovy.json.JsonGenerator



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

    private final static String ERR = "ERR: "
    private final static String OK = "OK"
    private final static String UNSUPPORTED_METHOD = "Unsupported method"
    private final static String INVALID_MESSAGE = "Invalid message"
    private final static int OK_CODE = 0
    private final static int ERR_CODE = -1
    private final static int UNSUPPORTED_METHOD_CODE = -2
    private final static int INVALID_MESSAGE_CODE = -3
    private static final JsonGenerator JSON = new JsonGenerator.Options().disableUnicodeEscaping().build()

    private static final Map<String, HikariDataSource> DATA_SOURCES = new ConcurrentHashMap<>()
    private static final Map<String, Sql> SQL_EXECUTORS = new ConcurrentHashMap<>()


    Response registerDb(DataSourceInfo info){
        try {
            String link = "${info.getUrl()}:${info.getUsername()}:${info.getPassword()}"
            String md5 = link.bytes.md5()
            if (DATA_SOURCES[md5]) {
                return Response.newBuilder().putData("res", md5).setCode(0).setMessage("OK").build()
            }
            HikariDataSource ds = new HikariDataSource()
            // 根据URL自动设置驱动类
            ds.setDriverClassName(info.getType())
            ds.setJdbcUrl(info.getUrl())
            ds.setUsername(info.getUsername())
            ds.setPassword(info.getPassword())
            if (info.getMaxPoolSize() > 0) ds.setMaximumPoolSize(info.getMaxPoolSize())
            if (info.getMinPoolSize() > 0) ds.setMinimumIdle(info.getMinPoolSize())
            if (info.getIdleTimeout() > 0) ds.setIdleTimeout(info.getIdleTimeout())
            if (info.getConnectionTimeout() > 0) ds.setConnectionTimeout(info.getConnectionTimeout())
            info.getOtherMap().forEach { k, v -> ds.addDataSourceProperty(k, v) }
            DATA_SOURCES[md5] = ds
            def builder = Response.newBuilder()
            builder.putData("res", md5)
            builder.setCode(OK_CODE).setMessage(OK)
            if (info.hasExec()) builder.setExec(info.getExec())
            return builder.build()
        } catch (Exception e) {
            def builder = Response.newBuilder().setCode(ERR_CODE).setMessage("$ERR: ${e.message}")
            if (info.hasExec()) builder.setExec(info.getExec())
            return builder.build()
        }
    }

    Response removeDb(DataSourceInfo info){
        String md5
        info.getOtherMap().each { k, v ->
            if (k == "key") {
                md5 = v
            }
        }
        if(!md5){
            String link = "${info.getUrl()}:${info.getUsername()}:${info.getPassword()}"
            md5 = link.bytes.md5()
        }
        if (DATA_SOURCES[md5]) {
            def i = 0
            while (i < 5){
                Thread.sleep(100)
                DATA_SOURCES[md5].close()
                if (!DATA_SOURCES[md5].closed){
                    break
                }
                i++
            }
            DATA_SOURCES.remove(md5)
            SQL_EXECUTORS.remove(md5)
            return Response.newBuilder().setCode(OK_CODE).setMessage(OK).build()
        }
        return Response.newBuilder().setCode(OK_CODE).setMessage(OK).build()
    }

    Response execSql(DataSourceInfo info){
        String md5
        info.getOtherMap().each { k, v ->
            if (k == "key") {
                md5 = v
            }
        }
        if(!md5){
            String link = "${info.getUrl()}:${info.getUsername()}:${info.getPassword()}"
            md5 = link.bytes.md5()
        }

        if (!DATA_SOURCES[md5]) {
            return Response.newBuilder().setCode(ERR_CODE).setMessage("$ERR: no datasource, can't execute").build()
        }
        Sql sql = SQL_EXECUTORS[md5]
        if (!sql) {
            sql = new Sql(DATA_SOURCES[md5] as DataSource)
            SQL_EXECUTORS[md5] = sql
        }
        def sqlStr = info.getOtherOrDefault("sql", "")
        if(!sqlStr){
            return Response.newBuilder().setCode(ERR_CODE).setMessage("$ERR: no sql").build()
        }
        def params = info.getOtherOrDefault("params", "")
        def exec = info.getOtherOrDefault("exec", "")
        def execPlus = info.getOtherOrDefault("execPlus", "")
        def execCode = info.getOtherOrDefault("execCode", "")
        if(execPlus){
            if(!execCode){
                return Response.newBuilder().setCode(ERR_CODE).setMessage("$ERR: no execCode").build()
            }
            def code = new GroovyShell().evaluate(execCode)
            if(code instanceof Closure){
                def cl = code as Closure
                cl.setResolveStrategy(Closure.DELEGATE_FIRST)
                cl.setDelegate(sql)
                cl.call()
            }
        }
        if(!exec){
            return Response.newBuilder().setCode(ERR_CODE).setMessage("$ERR: no exec").build()
        }
        def conditionVal = null
        if(params){
            try {
                conditionVal = new JsonSlurper().parseText(params)
            }catch (Exception e){
                log.info("params is not json")
            }
        }
        def res = null
        if(conditionVal instanceof Map){
            res = switch (exec){
                case "rows" -> sql.rows(conditionVal as Map, sqlStr)
                case "execute" -> sql.execute(conditionVal as Map, sqlStr)
            }
        }else if (conditionVal instanceof List){
            res = switch (exec){
                case "rows" -> sql.rows(sqlStr, conditionVal as List)
                case "execute" -> sql.execute(sqlStr, conditionVal as List)
            }
        }else {
            if(conditionVal){
                res = switch (exec){
                    case "rows" -> sql.rows(sqlStr, conditionVal)
                    case "execute" -> sql.execute(sqlStr, conditionVal)
                }
            }else {
                res = switch (exec){
                    case "rows" -> sql.rows(sqlStr)
                    case "execute" -> sql.execute(sqlStr)
                }
            }
        }
        def builder = Response.newBuilder().setCode(OK_CODE).setMessage(OK)
        if(res){
            builder.putData("res", JSON.toJson(res))
        }

        return builder.build()
    }


    Response handle(byte[] bytes) {
        try {
            DataSourceInfo info = DataSourceInfo.parseFrom(bytes)
            if (info.hasExec() && "registerDb" == info.getExec().getMethod()) {
                return registerDb(info)
            } else if (info.hasExec() && "removeDb" == info.getExec().getMethod()) {
                return removeDb(info)
            } else if (info.hasExec() && "execSql" == info.getExec().getMethod()) {
                return execSql(info)
            } else {
                def builder = Response.newBuilder()
                if (info.hasExec()) builder.setExec(info.getExec())
                return builder.setCode(UNSUPPORTED_METHOD_CODE)
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
