package com.yuezm.project.sql

import groovy.sql.Sql

import java.sql.Connection
import java.util.function.BiFunction
import java.util.function.Function


/**
 * SqlHandler
 *
 * @author yzm
 * @version 1.0
 * @description SqlHandler
 * @date 2025/6/20 9:53
 */
abstract class SqlHandler {

    protected  Connection connection;


    static Closure sqlHandlerFactory = null
    static BiFunction<String,Connection, SqlHandler> sqlHandlerFactory2 = null


    @Delegate
    Sql sql = null;


    public SqlHandler(Connection connection) {
        this.connection = connection
        sql = new Sql(connection)
    }
    protected SqlHandler() {

    }

    static SqlHandler buildSql(String type, Connection connection){
        assert connection != null : "connection is null"
        assert type != null : "type is null"
        assert type.trim() != "" : "type is empty"
        if(sqlHandlerFactory){
            return sqlHandlerFactory(type, connection)
        }
        if(sqlHandlerFactory2){
            return sqlHandlerFactory2.apply(type, connection)
        }
        return null
    }


    abstract boolean isSupportGis()


    /**
     * 字段类型映射
     * @param javaType
     * @return
     */
    abstract String mapJavaToSQL(String javaType)


    /**
     * 添加表备注
     * @param tableName
     * @param memo
     * @return
     */
    abstract boolean addTableMemo(String tableName, String memo)

}
