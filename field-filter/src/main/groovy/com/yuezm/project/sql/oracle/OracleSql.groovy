package com.yuezm.project.sql.oracle

import com.yuezm.project.sql.SqlHandler
import com.yuezm.project.sql.Wrapper

import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement

/**
 * OracleSql
 *
 * @author yzm
 * @version 1.0
 * @description oracle
 * @date 2025/7/31 10:38
 */
class OracleSql extends SqlHandler {

    OracleSql(Connection connection){
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
                sqlType = "NUMBER"
                break
            case "java.lang.Long":
            case "Long":
                sqlType = "NUMBER"
                break
            case "java.lang.Short":
            case "Short":
                sqlType = "NUMBER"
                break
            case "java.lang.Float":
            case "Float":
                sqlType = "FLOAT"
                break
            case "java.lang.Double":
            case "Double":
                sqlType = "FLOAT"
                break
            case "BigDecimal":
                sqlType = "NUMBER"
                break
            case "java.lang.Boolean":
            case "Boolean":
                sqlType = "boolean"
                break
            case "java.lang.String":
            case "String":
                sqlType = "VARCHAR2(1024)"
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
                sqlType = "DATE"
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
                sqlType = "CLOB"
                break
            default:
                throw new IllegalArgumentException("Unsupported Java type: ${javaType}")
        }
        return sqlType
    }

    @Override
    boolean addTableMemo(String tableName, String memo) {
        String sql = "COMMENT ON TABLE $tableName IS '$memo'"
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
        String sql = "DROP TABLE $tableName"
        try {
            execute(sql)
        }catch (Exception e) {
            e.printStackTrace()
            return false
        }
        return true
    }

    @Override
    Wrapper getWrapper() {
        if(selfWrapper == null){
            selfWrapper = new OracleSqlWrapper()
        }
        return selfWrapper
    }


    boolean execute(String sql) throws SQLException {
        if(sql?.contains(";")){
            sql.split(";(?=(?:[^']*'[^']*')*[^']*\$)").collect { it.trim()}.findAll { it}.each {
                super.execute(it)
            }
        }
        return true
    }

    @Override
    Number getTableDataCapacity(String tableName, String schema = null) {
        String sql = " SELECT \n" +
                "    SUM(bytes)  size_mb\n" +
                "FROM \n" +
                "    user_segments\n" +
                "WHERE \n" +
                "    segment_name = '$tableName'\n" +
                "GROUP BY \n" +
                "    segment_name"
        return firstRow(sql)?["size_mb"] as Number
    }

    @Override
    List<Map<String, Object>> getTablePrimarys(String tableName, String schema = null) {
        String sql = "SELECT cols.column_name\n" +
                "FROM user_constraints cons\n" +
                "JOIN user_cons_columns cols\n" +
                "  ON cons.constraint_name = cols.constraint_name\n" +
                "WHERE cons.constraint_type = 'P'\n" +
                "  AND cons.table_name = UPPER('$tableName')"
        return rows(sql)
    }
}
