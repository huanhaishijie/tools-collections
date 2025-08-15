package com.yuezm.project.sql.mysql

import com.yuezm.project.sql.SqlHandler
import com.yuezm.project.sql.Wrapper
import com.yuezm.project.sql.dm.DmWrapper

import java.sql.Connection


/**
 * MySql
 *
 * @author yzm
 * @version 1.0
 * @description mysql
 * @date 2025/7/31 10:38
 */
class MySql extends SqlHandler {
    MySql(Connection connection) {
        super(connection)
    }

    @Override
    boolean isSupportGis() {
        return false
    }

    @Override
    String mapJavaToSQL(String javaType) {
        String sqlType
        switch (javaType) {
            case "java.lang.Integer":
            case "Integer":
                sqlType = "INT"
                break
            case "java.lang.Long":
            case "Long":
                sqlType = "BIGINT"
                break
            case "java.lang.Short":
            case "Short":
                sqlType = "TINYINT"
                break
            case "java.lang.Float":
            case "Float":
                sqlType = "FLOAT(10,10)"
                break
            case "java.lang.Double":
            case "Double":
                sqlType = "DOUBLE(10,10)"
                break
            case "BigDecimal":
                sqlType = "DECIMAL(10,4)"
                break
            case "java.lang.Boolean":
            case "Boolean":
                sqlType = "BOOLEAN"
                break
            case "java.lang.String":
            case "String":
                sqlType = "VARCHAR(1024)"
                break
            case "java.lang.Character":
            case "Character":
                sqlType = "CHAR(10)"
                break
            case "java.time.LocalDate":
            case "LocalDate":
                sqlType = "DATE"
                break
            case "java.time.LocalTime":
            case "LocalTime":
                sqlType = "DATETIME"
                break
            case "java.time.LocalDateTime":
            case "java.util.Date":
            case "Date":
            case "LocalDateTime":
                sqlType = "TIMESTAMP"
                break
            case "byte[]":
            case "java.sql.Blob":

                sqlType = "BLOB"
                break
            case "java.util.UUID":
            case "UUID":
                sqlType = "uuid"
                break
            case "java.util.List":
            case "java.util.ArrayList":
            case "java.util.Set":
            case "java.util.HashSet":
            case "java.util.TreeSet":
            case "java.util.LinkedHashSet":
            case "List":
            case "ArrayList":
            case "Set":
            case "HashSet":
            case "TreeSet":
            case "LinkedHashSet":
                sqlType = "JSON"
                break
            case "java.util.Map":
            case "java.util.HashMap":
            case "java.util.TreeMap":
            case "java.util.LinkedHashMap":
            case "Map":
            case "HashMap":
            case "TreeMap":
            case "LinkedHashMap":
                sqlType = "JSON"
                break
            case "Geometry":
            case "geometry":
                sqlType = "TEXT"
                break
            default:
                throw new IllegalArgumentException("Unsupported Java type: ${javaType}")
        }
        return sqlType
    }

    @Override
    boolean addTableMemo(String tableName, String memo) {
        String sql = "ALTER TABLE ${tableName} COMMENT '${memo}'"
        try {
            execute(sql)
            return true
        } catch (Exception e) {
            e.printStackTrace()
        }
        return false
    }

    @Override
    boolean dropTable(String tableName) {
        String sql = "DROP TABLE IF EXISTS ${tableName}"
        try {
            execute(sql)
        } catch (Exception e) {
            e.printStackTrace()
            return false
        }
        return true
    }

    @Override
    Wrapper getWrapper() {
        if(selfWrapper == null){
            selfWrapper = new MySqlWrapper()
        }
        return wrapper
    }
}
