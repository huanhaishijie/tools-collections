package com.yuezm.project.sql.Kingbase

import com.yuezm.project.sql.SqlHandler
import com.yuezm.project.sql.Wrapper

import java.sql.Connection


/**
 * RdjcSql
 *
 * @author yzm
 * @version 1.0
 * @description 人大金仓
 * @date 2025/7/31 10:39
 */
class KingbaseSql extends SqlHandler{

    KingbaseSql(Connection connection){
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
                sqlType = "int4"
                break
            case "java.lang.Long":
            case "Long":
                sqlType = "int8"
                break
            case "java.lang.Short":
            case "Short":
                sqlType = "int2"
                break
            case "java.lang.Float":
            case "Float":
                sqlType = "float4"
                break
            case "java.lang.Double":
            case "Double":
                sqlType = "float8"
                break
            case "BigDecimal":
                sqlType = "numeric"
                break
            case "java.lang.Boolean":
            case "Boolean":
                sqlType = "boolean"
                break
            case "java.lang.String":
            case "String":
                sqlType = "varchar(1024)"
                break
            case "java.lang.Character":
            case "Character":
                sqlType = "char"
                break
            case "java.time.LocalDate":
            case "LocalDate":
                sqlType = "date"
                break
            case "java.time.LocalTime":
            case "LocalTime":
                sqlType = "time"
                break
            case "java.time.LocalDateTime":
            case "java.util.Date":
            case "Date":
            case "LocalDateTime":
                sqlType = "timestamp"
                break
            case "byte[]":
            case "java.sql.Blob":

                sqlType = "bytea"
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
                sqlType = "jsonb"
                break
            case "java.util.Map":
            case "java.util.HashMap":
            case "java.util.TreeMap":
            case "java.util.LinkedHashMap":
            case "Map":
            case "HashMap":
            case "TreeMap":
            case "LinkedHashMap":
                sqlType = "jsonb"
                break
            case "Geometry":
            case "geometry":
                sqlType = "text"
                break
            default:
                throw new IllegalArgumentException("Unsupported Java type: ${javaType}")
        }
        return sqlType
    }

    @Override
    boolean addTableMemo(String tableName, String memo) {
        String sql = "COMMENT ON TABLE $tableName IS '$memo';"
        try {
            execute(sql)
        }catch (Exception e) {
            e.printStackTrace()
            return false
        }
        return true
    }

    @Override
    boolean dropTable(String tableName) {
        String sql = "DROP TABLE IF EXISTS $tableName;"
        try {
            execute(sql)
        }catch (Exception e) {
            e.printStackTrace()
        }
        return true
    }

    @Override
    Wrapper getWrapper() {
        return new KingbaseWrapper()
    }
}
