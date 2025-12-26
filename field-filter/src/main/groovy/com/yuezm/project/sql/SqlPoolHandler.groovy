package com.yuezm.project.sql

import com.yuezm.project.connector.Client
import com.yuezm.project.connector.proto.DataSourceInfo
import com.yuezm.project.connector.proto.ExecInfo
import com.yuezm.project.connector.proto.RequestInfo
import groovy.json.JsonGenerator
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * SqlPoolHandler
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/12/22 10:05
 */
abstract class SqlPoolHandler extends SqlHandler{

    protected static String CLIENT_URL_PREFIX = "aeron:udp?endpoint="

    protected DatasourceProperties datasourceProperties
    protected Map<String, Object> prop
    protected String key
    protected static final Map<String, SqlPoolHandler> pool = [:]
    protected volatile static Client client
    def jsonSlurper = new JsonSlurper()
    private static final JsonGenerator JSON = new JsonGenerator.Options().disableUnicodeEscaping().build()
    protected static synchronized void initClient(){
        if(!client){
            PoolConfig poolConfig = PoolConfig.instance
            client = Client.getInstance(poolConfig.clientHost, poolConfig.clientPort, poolConfig.clientSteamId, poolConfig.serverHost)
        }
    }



    SqlPoolHandler(DatasourceProperties datasourceProperties, Map<String, Object> otherPoolConfig = [:]) {
        assert datasourceProperties != null : "datasourceProperties is null"
        assert datasourceProperties.url != null : "datasourceProperties.url is null"
        assert datasourceProperties.username != null : "datasourceProperties.username is null"
        assert datasourceProperties.password != null : "datasourceProperties.password is null"
        assert datasourceProperties.driverClassName != null : "datasourceProperties.driverClassName is null"
        this.datasourceProperties = datasourceProperties
        this.prop = otherPoolConfig
        String link = "${datasourceProperties.getUrl()}:${datasourceProperties.getUsername()}:${datasourceProperties.getPassword()}"
        def key = link.bytes.md5()
        this.key = key
        initClient()
        if(pool[key]){

        }else {
            String resKey = registerDb()
            if(resKey != key){
                throw new RuntimeException("registerDb failed, key: $key, resKey: $resKey")
            }
            pool.put(key, this)
        }
    }


    protected String registerDb(){
        PoolConfig poolConfig = PoolConfig.instance
        def responseFuture = new CompletableFuture<String>()
        def latch = new CountDownLatch(1)
        def reply = "${CLIENT_URL_PREFIX}${poolConfig.clientHost}:${poolConfig.clientPort}".toString()
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel(reply)
                                .setReplyStream(poolConfig.clientSteamId)
                                .build()
                ).setMethod("registerDb").build()
        ).setUrl(datasourceProperties.url)
                .setType(datasourceProperties.driverClassName)
                .setUsername(datasourceProperties.username)
                .setPassword(datasourceProperties.password)
                .setMaxPoolSize(prop?.maxPoolSize as Integer ?:20)
                .setMinPoolSize(prop?.minPoolSize as Integer ?:3)
                .setIdleTimeout(prop?.idleTimeout as Integer ?:60000)
                .setConnectionTimeout(prop?.idleTimeout as Integer ?:60000)
                .build()
        client.send dataSourceInfo, { backInfo ->
            println "Received response: ${backInfo?.newValue}"
            responseFuture.complete(backInfo?.newValue)
            latch.countDown()
        }
        try {
            // Wait for the response with a timeout of 10 minutes
            return responseFuture.get(10, TimeUnit.MINUTES)
        } catch (TimeoutException e) {
            // If we get here, the timeout occurred
            throw new RuntimeException("Registration timeout: No response received within 10 minutes", e)
        } catch (Exception e) {
            // Handle other potential exceptions
            throw new RuntimeException("Error during database registration", e)
        } finally {
            // Ensure we clean up in case of any issues
            if (!responseFuture.isDone()) {
                responseFuture.cancel(true)
            }
        }
    }

    
    GroovyRowResult firstRow(String sql) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                //服务端回信通道
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key).putOther("exec", "firstRow").putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        def map = jsonSlurper.parseText(res) as Map<String, Object>
        return new GroovyRowResult(map)
    }

    
    GroovyRowResult firstRow(GString gstring) {
        return firstRow(gstring.toString())
    }

    
    GroovyRowResult firstRow(String sql, List<Object> params) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("exec", "firstRow")
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        def map = jsonSlurper.parseText(res) as Map<String, Object>
        return new GroovyRowResult(map)
    }

    
    GroovyRowResult firstRow(Map params, String sql) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("exec", "firstRow")
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        def map = jsonSlurper.parseText(res) as Map<String, Object>
        return new GroovyRowResult(map)
    }

    
    GroovyRowResult firstRow(String sql, Object[] params) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("exec", "firstRow")
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        def map = jsonSlurper.parseText(res) as Map<String, Object>
        return new GroovyRowResult(map)
    }


    protected String sqlExec(DataSourceInfo dataSourceInfo){
        def responseFuture = new CompletableFuture<String>()
        def latch = new CountDownLatch(1)
        client.send dataSourceInfo, { backInfo ->
            println "Received response: ${backInfo?.newValue}"
            responseFuture.complete(backInfo?.newValue)
            latch.countDown()
        }
        try {
            // Wait for the response with a timeout of 10 minutes
            return responseFuture.get(30, TimeUnit.MINUTES)
        } catch (TimeoutException e) {
            // If we get here, the timeout occurred
            throw new RuntimeException("Registration timeout: No response received within 10 minutes", e)
        } catch (Exception e) {
            // Handle other potential exceptions
            throw new RuntimeException("Error during database registration", e)
        } finally {
            // Ensure we clean up in case of any issues
            if (!responseFuture.isDone()) {
                responseFuture.cancel(true)
            }
        }

    }

    protected void sqlExec2(DataSourceInfo dataSourceInfo){
        client.send dataSourceInfo, { backInfo ->
            println "Received response: ${backInfo?.newValue}"
        }

    }

    
    boolean execute(String sql) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("exec", "execute")
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        return Boolean.valueOf(res)
    }

    
    @Deprecated
    void execute(String sql, Closure processResults) {
        throw new UnsupportedOperationException("pool execute not support Closure")
    }

    void execute(String sql, String execCode) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("execPlus", "execPlus")
                .putOther("execCode", execCode)
                .putOther("sql", sql).build()
        sqlExec2(dataSourceInfo)
    }

    
    boolean execute(String sql, List<Object> params) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("exec", "execute")
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        return Boolean.valueOf(res)
    }

    
    @Deprecated
    void execute(String sql, List<Object> params, Closure processResults) {
        throw new UnsupportedOperationException("pool execute not support Closure")
    }

    void execute(String sql, List<Object> params, String execCode) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("execPlus", "execPlus")
                .putOther("execCode", execCode)
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        sqlExec2(dataSourceInfo)
    }

    
    boolean execute(Map params, String sql) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("exec", "execute")
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        return Boolean.valueOf(res)
    }

    
    @Deprecated
    void execute(Map params, String sql, Closure processResults) {
        throw new UnsupportedOperationException("pool execute not support Closure")
    }

    void execute(Map params, String sql, String execCode) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("execPlus", "execPlus")
                .putOther("execCode", execCode)
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        sqlExec2(dataSourceInfo)
    }

    
    boolean execute(String sql, Object[] params) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("exec", "execute")
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        return Boolean.valueOf(res)
    }

    
    @Deprecated
    void execute(String sql, Object[] params, Closure processResults) {
        throw new UnsupportedOperationException("pool execute not support Closure")
    }

    void execute(String sql, Object[] params, String execCode) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("execPlus", "execPlus")
                .putOther("execCode", execCode)
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        sqlExec2(dataSourceInfo)
    }

    
    boolean execute(GString gstring) {
        return execute(gstring.toString())
    }

    
    @Deprecated
    void execute(GString gstring, Closure processResults) {
        throw new UnsupportedOperationException("pool execute not support Closure")
    }

    void execute(GString gstring, String execCode) {
        execute(gstring.toString(), execCode)
    }

    
    List<GroovyRowResult> rows(String sql) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("exec", "rows")
                .putOther("sql", sql)
                .build()
        def res = sqlExec(dataSourceInfo)
        def list = jsonSlurper.parseText(res) as List<Map>
        return list?.collect { new GroovyRowResult(it) }
    }

    
    @Deprecated
    List<GroovyRowResult> rows(String sql, int offset, int maxRows) {
        throw new UnsupportedOperationException("pool rows not support offset and maxRows")
    }

    
    @Deprecated
    List<GroovyRowResult> rows(String sql, Closure metaClosure) {
        throw new UnsupportedOperationException("pool rows not support Closure")
    }


    List<GroovyRowResult> rows(String sql, String execCode) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("execPlus", "execPlus")
                .putOther("execCode", execCode)
//                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        def list = jsonSlurper.parseText(res) as List<Map>
        return list?.collect { new GroovyRowResult(it) }
    }



    
    @Deprecated
    List<GroovyRowResult> rows(String sql, int offset, int maxRows, Closure metaClosure) {
        throw new UnsupportedOperationException("pool rows not support offset and maxRows")
    }

    
    List<GroovyRowResult> rows(String sql, List<Object> params) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("exec", "rows")
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql)
                .build()
        def res = sqlExec(dataSourceInfo)
        def list = jsonSlurper.parseText(res) as List<Map>
        return list?.collect { new GroovyRowResult(it) }
    }

    
    List<GroovyRowResult> rows(Map params, String sql) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("exec", "rows")
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql)
                .build()
        def res = sqlExec(dataSourceInfo)
        def list = jsonSlurper.parseText(res) as List<Map>
        return list?.collect { new GroovyRowResult(it) }
    }

    
    @Deprecated
    List<GroovyRowResult> rows(String sql, List<Object> params, int offset, int maxRows) {
        throw new UnsupportedOperationException("pool rows not support offset and maxRows")
    }

    
    @Deprecated
    List<GroovyRowResult> rows(String sql, Map params, int offset, int maxRows) {
        throw new UnsupportedOperationException("pool rows not support offset and maxRows")
    }

    
    @Deprecated
    List<GroovyRowResult> rows(Map params, String sql, int offset, int maxRows) {
        throw new UnsupportedOperationException("pool rows not support offset and maxRows")
    }

    
    List<GroovyRowResult> rows(String sql, Object[] params) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("exec", "rows")
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql)
                .build()
        def res = sqlExec(dataSourceInfo)
        def list = jsonSlurper.parseText(res) as List<Map>
        return list?.collect { new GroovyRowResult(it) }
    }

    
    @Deprecated
    List<GroovyRowResult> rows(String sql, Object[] params, int offset, int maxRows) {
        throw new UnsupportedOperationException("pool rows not support offset and maxRows")
    }

    
    @Deprecated
    List<GroovyRowResult> rows(String sql, List<Object> params, Closure metaClosure) {
        throw new UnknownFormatFlagsException("metaClosure")
    }

    List<GroovyRowResult> rows(String sql, List<Object> params, String execCode) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("execPlus", "execPlus")
                .putOther("execCode", execCode)
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        def list = jsonSlurper.parseText(res) as List<Map>
        return list?.collect { new GroovyRowResult(it) }
    }

    
    @Deprecated
    List<GroovyRowResult> rows(String sql, Map params, Closure metaClosure) {
        throw new UnsupportedOperationException("pool rows not support Closure")
    }

    List<GroovyRowResult> rows(String sql, Map params, String execCode) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("execPlus", "execPlus")
                .putOther("execCode", execCode)
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        def list = jsonSlurper.parseText(res) as List<Map>
        return list?.collect { new GroovyRowResult(it) }
    }

    
    @Deprecated
    List<GroovyRowResult> rows(Map params, String sql, Closure metaClosure) {
        throw new UnsupportedOperationException("pool rows not support Closure")
    }

    List<GroovyRowResult> rows(Map params, String sql, String execCode) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("execPlus", "execPlus")
                .putOther("execCode", execCode)
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        def list = jsonSlurper.parseText(res) as List<Map>
        return list?.collect { new GroovyRowResult(it) }
    }

    
    @Deprecated
    List<GroovyRowResult> rows(String sql, List<Object> params, int offset, int maxRows, Closure metaClosure) {
        throw new UnsupportedOperationException("pool rows not support Closure")
    }

    
    @Deprecated
    List<GroovyRowResult> rows(String sql, Map params, int offset, int maxRows, Closure metaClosure) {
        throw new UnsupportedOperationException("pool rows not support Closure")
    }

    
    @Deprecated
    List<GroovyRowResult> rows(Map params, String sql, int offset, int maxRows, Closure metaClosure) {
        throw new UnsupportedOperationException("pool rows not support Closure")
    }

    
    @Deprecated
    List<GroovyRowResult> rows(GString sql, int offset, int maxRows) {
        throw new UnsupportedOperationException("pool rows not support Closure")
    }

    
    List<GroovyRowResult> rows(GString gstring) {
        return rows(gstring.toString())
    }

    
    @Deprecated
    List<GroovyRowResult> rows(GString gstring, Closure metaClosure) {
        throw new UnsupportedOperationException("pool rows not support Closure")
    }

    List<GroovyRowResult> rows(GString gstring, String execCode) {
        return rows(gstring.toString(), execCode)
    }

    
    @Deprecated
    List<GroovyRowResult> rows(GString gstring, int offset, int maxRows, Closure metaClosure) {
        throw new UnsupportedOperationException("pool rows not support Closure")
    }

    
    @Deprecated
    void query(String sql, Closure closure) {
        throw new UnsupportedOperationException("pool rows not support Closure")
    }

    <R> R query(String sql, String execCode) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("execPlus", "execPlus")
                .putOther("execCode", execCode)
//                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        def resData = jsonSlurper.parseText(res)
        if(resData instanceof List){
            return (R) resData.collect { new GroovyRowResult(it as Map) }
        }else{
            return (R) new GroovyRowResult(resData as Map)
        }
    }


    
    @Deprecated
    void query(String sql, List<Object> params, Closure closure) {
        throw new UnsupportedOperationException("pool rows not support Closure")
    }

    <R> R query(String sql, List<Object> params, String execCode) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("execPlus", "execPlus")
                .putOther("execCode", execCode)
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        def resData = jsonSlurper.parseText(res)
        if(resData instanceof List){
            return (R) resData.collect { new GroovyRowResult(it as Map) }
        }else{
            return (R) new GroovyRowResult(resData as Map)
        }
    }

    
    @Deprecated
    void query(String sql, Map map, Closure closure) {
        throw new UnsupportedOperationException("pool rows not support Closure")
    }


    <R> R query(String sql, Map map, String execCode) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("execPlus", "execPlus")
                .putOther("execCode", execCode)
                .putOther("params", JSON.toJson(map))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        def resData = jsonSlurper.parseText(res)
        if(resData instanceof List){
            return (R) resData.collect { new GroovyRowResult(it as Map) }
        }else{
            return (R) new GroovyRowResult(resData as Map)
        }
    }

    
    @Deprecated
    void query(Map map, String sql, Closure closure) {
        throw new UnsupportedOperationException("pool rows not support Closure")
    }

    <R> R query(Map map, String sql, String execCode) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("execPlus", "execPlus")
                .putOther("execCode", execCode)
                .putOther("params", JSON.toJson(map))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        def resData = jsonSlurper.parseText(res)
        if(resData instanceof List){
            return (R) resData.collect { new GroovyRowResult(it as Map) }
        }else{
            return (R) new GroovyRowResult(resData as Map)
        }
    }


    
    @Deprecated
    void query(GString gstring, Closure closure) {
        throw new UnsupportedOperationException("pool rows not support Closure")
    }


    <R> R query(GString gstring, String execCode) {
        query(gstring.toString(), execCode)
    }

    @Override
    <R> R expExec(Object params, String exec) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("execPlus", "execPlus")
                .putOther("execCode", exec)
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        def res = sqlExec(dataSourceInfo)
        def resData = jsonSlurper.parseText(res)
        if(resData instanceof List){
            return (R) resData.collect { new GroovyRowResult(it as Map) }
        }else{
            return (R) new GroovyRowResult(resData as Map)
        }
    }

    @Override
    void exp2Exec(Object params, String exec) {
        def dataSourceInfo = DataSourceInfo.newBuilder().setExec(
                ExecInfo.newBuilder().setRequestInfo(
                        RequestInfo.newBuilder()
                                .setReplyChannel("aeron:udp?endpoint=$PoolConfig.instance.clientHost:$PoolConfig.instance.clientPort".toString())
                                .setReplyStream(PoolConfig.instance.clientSteamId)
                                .build()
                ).setMethod("execSql").build()
        ).putOther("key", key)
                .putOther("execPlus", "execPlus")
                .putOther("execCode", exec)
                .putOther("params", JSON.toJson(params))
                .putOther("sql", sql).build()
        sqlExec2(dataSourceInfo)
    }
}
