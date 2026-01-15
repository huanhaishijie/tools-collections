package com.yuezm.project.sql

import com.zaxxer.hikari.HikariDataSource
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


/**
 * SqlPoolHandler
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/12/22 10:05
 */
abstract class SqlLocalPoolHandler extends SqlHandler{

    protected Map<String, Object> prop
    protected DatasourceProperties datasourceProperties
    protected String key

    protected static  ConcurrentMap<String, HikariDataSource> cacheDataSource = new ConcurrentHashMap()

    SqlLocalPoolHandler(DatasourceProperties datasourceProperties, Map<String, Object> otherPoolConfig = [:]) {
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
        if(!cacheDataSource[key]){
            HikariDataSource ds = null
            try {
                ds = new HikariDataSource()
                ds.setDriverClassName(datasourceProperties.driverClassName)
                ds.setJdbcUrl(datasourceProperties.url)
                ds.setUsername(datasourceProperties.username)
                ds.setPassword(datasourceProperties.password)
                if(otherPoolConfig?.maxPoolSize){
                    ds.setMaximumPoolSize(otherPoolConfig?.maxPoolSize as Integer)
                }
                if(otherPoolConfig?.minPoolSize){
                    ds.setMinimumIdle(otherPoolConfig?.minPoolSize as Integer)
                }
                if(otherPoolConfig?.idleTimeout){
                    ds.setIdleTimeout(otherPoolConfig?.idleTimeout as Long)
                }
                if(otherPoolConfig?.connectionTimeout){
                    ds.setConnectionTimeout(otherPoolConfig?.connectionTimeout as Long)
                }

                // 连接池稳定性配置
                ds.setLeakDetectionThreshold(60_000)
                ds.setValidationTimeout(5_000)
                ds.setMaxLifetime(30 * 60 * 1000)
                prop.each { k, v ->
                    ds.addDataSourceProperty(k, v)
                }
                verifyDataSource(ds)
                cacheDataSource[key] = ds
            }catch (Exception e){
                throw new IllegalArgumentException("init datasource failed", e)
            }
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


    private static <T> T withSql(String key, Closure<T> action) {
        HikariDataSource ds = cacheDataSource[key]
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
                } catch (Exception e) {}
            }
        }
    }


    void query(String sql, Closure closure) {
        withSql(key, {Sql s -> s.query(sql, closure) })
    }


    void query(String sql, List<Object> params, Closure closure) {
        withSql(key, { Sql s -> s.query(sql, params, closure) })
    }


    void query(String sql, Map map, Closure closure) {
        withSql(key, { Sql s -> s.query(sql, map, closure) })
    }


    void query(Map map, String sql, Closure closure) {
        withSql(key, { Sql s -> s.query(map, sql, closure) })
    }


    void query(GString gstring, Closure closure) {
        withSql(key, { Sql s -> s.query(gstring, closure) })
    }


    void eachRow(String sql, Closure closure) {
        withSql(key, { Sql s -> s.eachRow(sql, closure) })
    }


    void eachRow(String sql, int offset, int maxRows, Closure closure) {
        withSql(key, {Sql s -> s.eachRow(sql, offset, maxRows, closure) })
    }


    void eachRow(String sql, Closure metaClosure, Closure rowClosure) {
        withSql(key, {Sql s -> s.eachRow(sql, metaClosure, rowClosure) })
    }


    void eachRow(String sql, Closure metaClosure, int offset, int maxRows, Closure rowClosure) {
       withSql(key, {Sql s -> s.eachRow(sql, metaClosure, offset, maxRows, rowClosure) })
    }


    void eachRow(String sql, List<Object> params, Closure metaClosure, int offset, int maxRows, Closure rowClosure) {
        withSql(key, {Sql s -> s.eachRow(sql, params, metaClosure, offset, maxRows, rowClosure) })
    }

    
    void eachRow(String sql, Map map, Closure metaClosure, int offset, int maxRows, Closure rowClosure) {
        withSql(key, {Sql s -> s.eachRow(sql, map, metaClosure, offset, maxRows, rowClosure) })
    }

    
    void eachRow(Map map, String sql, Closure metaClosure, int offset, int maxRows, Closure rowClosure) {
        withSql(key, {Sql s -> s.eachRow(map, sql, metaClosure, offset, maxRows, rowClosure) })
    }

    
    void eachRow(String sql, List<Object> params, Closure metaClosure, Closure rowClosure) {
        withSql(key, {Sql s -> s.eachRow(sql, params, metaClosure, rowClosure) })
    }

    
    void eachRow(String sql, Map params, Closure metaClosure, Closure rowClosure) {
        withSql(key, {Sql s -> s.eachRow(sql, params, metaClosure, rowClosure) })
    }

    
    void eachRow(Map params, String sql, Closure metaClosure, Closure rowClosure) {
        withSql(key, {Sql s -> s.eachRow(params, sql, metaClosure, rowClosure) })
    }

    
    void eachRow(String sql, List<Object> params, Closure closure) {
        withSql(key, {Sql s -> s.eachRow(sql, params, closure) })
    }

    
    void eachRow(String sql, Map params, Closure closure) {
        withSql(key, {Sql s -> s.eachRow(sql, params, closure) })
    }

    
    void eachRow(Map params, String sql, Closure closure) {
        withSql(key, {Sql s -> s.eachRow(params, sql, closure) })
    }

    
    void eachRow(String sql, List<Object> params, int offset, int maxRows, Closure closure) {
        withSql(key, {Sql s -> s.eachRow(sql, params, offset, maxRows, closure) })
    }

    
    void eachRow(String sql, Map params, int offset, int maxRows, Closure closure) {
        withSql(key, {Sql s -> s.eachRow(sql, params, offset, maxRows, closure) })
    }

    
    void eachRow(Map params, String sql, int offset, int maxRows, Closure closure) {
        withSql(key, {Sql s -> s.eachRow(params, sql, offset, maxRows, closure) })
    }

    
    void eachRow(GString gstring, Closure metaClosure, Closure rowClosure) {
        withSql(key, {Sql s -> s.eachRow(gstring, metaClosure, rowClosure) })
    }

    
    void eachRow(GString gstring, Closure metaClosure, int offset, int maxRows, Closure rowClosure) {
        withSql(key, {Sql s -> s.eachRow(gstring, metaClosure, offset, maxRows, rowClosure)})
    }

    
    void eachRow(GString gstring, int offset, int maxRows, Closure closure) {
        withSql(key, {Sql s -> s.eachRow(gstring, offset, maxRows, closure)})
    }

    
    void eachRow(GString gstring, Closure closure) {
        withSql(key, {Sql s -> s.eachRow(gstring, closure)})
    }

    
    List<GroovyRowResult> rows(String sql) {
        return withSql(key, {Sql s -> s.rows(sql)})
    }

    
    List<GroovyRowResult> rows(String sql, int offset, int maxRows) {
        return withSql(key, {Sql s -> s.rows(sql, offset, maxRows)})
    }

    
    List<GroovyRowResult> rows(String sql, Closure metaClosure) {
        return withSql(key, {Sql s -> s.rows(sql, metaClosure)})
    }

    
    List<GroovyRowResult> rows(String sql, int offset, int maxRows, Closure metaClosure) {
        return withSql(key, {Sql s -> s.rows(sql, offset, maxRows, metaClosure)})
    }

    
    List<GroovyRowResult> rows(String sql, List<Object> params) {
        return withSql(key, {Sql s -> s.rows(sql, params)})
    }

    
    List<GroovyRowResult> rows(Map params, String sql) {
        return withSql(key, {Sql s -> s.rows(params, sql)})
    }

    
    List<GroovyRowResult> rows(String sql, List<Object> params, int offset, int maxRows) {
        return withSql(key, {Sql s -> s.rows(sql, params, offset, maxRows)})
    }

    
    List<GroovyRowResult> rows(String sql, Map params, int offset, int maxRows) {
        return withSql(key, {Sql s -> s.rows(sql, params, offset, maxRows)})
    }

    
    List<GroovyRowResult> rows(Map params, String sql, int offset, int maxRows) {
        return withSql(key, {Sql s -> s.rows(params, sql, offset, maxRows)})
    }

    
    List<GroovyRowResult> rows(String sql, Object[] params) {
        return withSql(key, {Sql s -> s.rows(sql, params)})
    }

    
    List<GroovyRowResult> rows(String sql, Object[] params, int offset, int maxRows) {
        return withSql(key, {Sql s -> s.rows(sql, params, offset, maxRows)})
    }

    
    List<GroovyRowResult> rows(String sql, List<Object> params, Closure metaClosure) {
        return withSql(key, {Sql s -> s.rows(sql, params, metaClosure)})
    }

    
    List<GroovyRowResult> rows(String sql, Map params, Closure metaClosure) {
        return withSql(key, {Sql s -> s.rows(sql, params, metaClosure)})
    }

    
    List<GroovyRowResult> rows(Map params, String sql, Closure metaClosure) {
        return withSql(key, {Sql s -> s.rows(params, sql, metaClosure)})
    }

    
    List<GroovyRowResult> rows(String sql, List<Object> params, int offset, int maxRows, Closure metaClosure) {
        return withSql(key, {Sql s -> s.rows(sql, params, offset, maxRows, metaClosure)})
    }

    
    List<GroovyRowResult> rows(String sql, Map params, int offset, int maxRows, Closure metaClosure) {
        return withSql(key, {Sql s -> s.rows(sql, params, offset, maxRows, metaClosure)})
    }

    
    List<GroovyRowResult> rows(Map params, String sql, int offset, int maxRows, Closure metaClosure) {
        return withSql(key, {Sql s -> s.rows(params, sql, offset, maxRows, metaClosure)})
    }

    
    List<GroovyRowResult> rows(GString sql, int offset, int maxRows) {
        return withSql(key, {Sql s -> s.rows(sql, offset, maxRows)})
    }

    
    List<GroovyRowResult> rows(GString gstring) {
        return withSql(key, {Sql s -> s.rows(gstring)})
    }

    
    List<GroovyRowResult> rows(GString gstring, Closure metaClosure) {
        return withSql(key, {Sql s -> s.rows(gstring, metaClosure)})
    }

    
    List<GroovyRowResult> rows(GString gstring, int offset, int maxRows, Closure metaClosure) {
        return withSql(key, {Sql s -> s.rows(gstring, offset, maxRows, metaClosure)})
    }

    
    GroovyRowResult firstRow(String sql) {
        return withSql(key, {Sql s -> s.firstRow(sql)})
    }

    
    GroovyRowResult firstRow(GString gstring) {
        return withSql(key, {Sql s -> s.firstRow(gstring)})
    }

    
    GroovyRowResult firstRow(String sql, List<Object> params) {
        return withSql(key, {Sql s -> s.firstRow(sql, params)})
    }

    
    GroovyRowResult firstRow(Map params, String sql) {
        return withSql(key, {Sql s -> s.firstRow(params, sql)})
    }

    
    GroovyRowResult firstRow(String sql, Object[] params) {
        return withSql(key, {Sql s -> s.firstRow(sql, params)})
    }

    
    boolean execute(String sql) {
        return withSql(key, {Sql s -> s.execute(sql)})
    }

    
    void execute(String sql, Closure processResults) {
        withSql(key, {Sql s -> s.execute(sql, processResults)})
    }

    
    boolean execute(String sql, List<Object> params) {
        return withSql(key, {Sql s -> s.execute(sql, params)})
    }

    
    void execute(String sql, List<Object> params, Closure processResults) {
        withSql(key, {Sql s -> s.execute(sql, params, processResults)})
    }

    
    boolean execute(Map params, String sql) {
        return withSql(key, {Sql s -> s.execute(params, sql)})
    }

    
    void execute(Map params, String sql, Closure processResults) {
        withSql(key, {Sql s -> s.execute(params, sql, processResults)})
    }

    
    boolean execute(String sql, Object[] params) {
        return withSql(key, {Sql s -> s.execute(sql, params)})
    }

    
    void execute(String sql, Object[] params, Closure processResults) {
        withSql(key, {Sql s -> s.execute(sql, params, processResults)})
    }

    
    boolean execute(GString gstring) {
        return withSql(key, {Sql s -> s.execute(gstring)})
    }

    
    void execute(GString gstring, Closure processResults) {
        super.execute(gstring, processResults)
    }

    
    List<List<Object>> executeInsert(String sql) {
        return withSql(key, {Sql s -> s.executeInsert(sql)})
    }

    
    List<List<Object>> executeInsert(String sql, List<Object> params) {
        return withSql(key, {Sql s -> s.executeInsert(sql, params)})
    }

    
    List<GroovyRowResult> executeInsert(String sql, List<Object> params, List<String> keyColumnNames) {
        return withSql(key, {Sql s -> s.executeInsert(sql, params, keyColumnNames)})
    }

    
    List<List<Object>> executeInsert(Map params, String sql) {
        return withSql(key, {Sql s -> s.executeInsert(params, sql)})
    }

    
    List<GroovyRowResult> executeInsert(Map params, String sql, List<String> keyColumnNames) {
        return withSql(key, {Sql s -> s.executeInsert(params, sql, keyColumnNames)})
    }

    
    List<List<Object>> executeInsert(String sql, Object[] params) {
        return withSql(key, {Sql s -> s.executeInsert(sql, params)})
    }

    
    List<GroovyRowResult> executeInsert(String sql, String[] keyColumnNames) {
        return withSql(key, {Sql s -> s.executeInsert(sql, keyColumnNames)})
    }

    
    List<GroovyRowResult> executeInsert(String sql, String[] keyColumnNames, Object[] params) {
        return withSql(key, {Sql s -> s.executeInsert(sql, keyColumnNames, params)})
    }

    
    List<List<Object>> executeInsert(GString gstring) {
        return withSql(key, {Sql s -> s.executeInsert(gstring)})
    }

    
    List<GroovyRowResult> executeInsert(GString gstring, List<String> keyColumnNames) {
        return withSql(key, {Sql s -> s.executeInsert(gstring, keyColumnNames)})
    }

    
    int executeUpdate(String sql) {
        return withSql(key, {Sql s -> s.executeUpdate(sql)})
    }

    
    int executeUpdate(String sql, List<Object> params) {
        return withSql(key, {Sql s -> s.executeUpdate(sql, params)})
    }

    
    int executeUpdate(Map params, String sql) {
        return withSql(key, {Sql s -> s.executeUpdate(params, sql)})
    }

    
    int executeUpdate(String sql, Object[] params) {
        return withSql(key, {Sql s -> s.executeUpdate(sql, params)})
    }

    
    int executeUpdate(GString gstring) {
        return withSql(key, {Sql s -> s.executeUpdate(gstring)})
    }


    void close() {
        def ds = cacheDataSource.remove(key)
        if (ds) {
            try {
                ds.close()
            } catch (Exception e) {
                println "close datasource error ${e}"
            }
        }
    }
}
