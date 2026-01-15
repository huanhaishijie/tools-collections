package com.yuezm.project.sql.oracle

import com.yuezm.project.sql.DatasourceProperties
import com.yuezm.project.sql.SqlPoolHandler
import com.yuezm.project.sql.TableField
import com.yuezm.project.sql.TableInfo
import com.yuezm.project.sql.Wrapper

import java.sql.SQLException


/**
 * OracleSqlPool
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2026/1/7 9:12
 */
class OracleSqlPool extends SqlPoolHandler{
    OracleSqlPool(DatasourceProperties datasourceProperties, Map<String, Object> otherPoolConfig = [:]) {
        super(datasourceProperties, otherPoolConfig)
    }

    @Override
    Number getTableDataCapacity(String tableName, String schema) {
        String sql = " SELECT \n" +
                "    SUM(bytes)  size_mb\n" +
                "FROM \n" +
                "    user_segments\n" +
                "WHERE \n" +
                "    segment_name = UPPER('$tableName')\n" +
                "GROUP BY \n" +
                "    segment_name".toString()
        return firstRow(sql)?["size_mb"] as Number
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

    @Override
    List<Map<String, Object>> getTablePrimarys(String tableName, String schema) {
        String sql = "SELECT cols.column_name\n" +
                "FROM user_constraints cons\n" +
                "JOIN user_cons_columns cols\n" +
                "  ON cons.constraint_name = cols.constraint_name\n" +
                "WHERE cons.constraint_type = 'P'\n" +
                "  AND cons.table_name = UPPER('$tableName')".toString()
        return rows(sql)
    }

    @Override
    TableInfo getTableInfo(String tableName, String schema) {
        String sql = "SELECT \n" +
                "    TABLE_NAME, \n" +
                "    COMMENTS \n" +
                "FROM ALL_TAB_COMMENTS\n" +
                "WHERE TABLE_NAME = UPPER('$tableName')\n" +
                "  AND OWNER = UPPER('$schema')".toString()
        def row = firstRow(sql)
        if(row == null){
            return null
        }
        def t = new TableInfo(tableName: row?["TABLE_NAME"], comment: row?["COMMENTS"])
        sql = "SELECT \n" +
                "    COLUMN_NAME,\n" +
                "    DATA_TYPE,\n" +
                "    DATA_LENGTH,\n" +
                "    DATA_PRECISION,\n" +
                "    DATA_SCALE,\n" +
                "    NULLABLE,\n" +
                "    DATA_DEFAULT\n" +
                "FROM ALL_TAB_COLUMNS\n" +
                "WHERE TABLE_NAME = UPPER('$tableName')\n" +
                "  AND OWNER = UPPER('$schema')\n" +
                "ORDER BY COLUMN_ID".toString()
        def columns = rows(sql)
        t.fields = columns.collect { column ->
            return new TableField(
                    colName: column?["COLUMN_NAME"],
                    dataType: column?["DATA_TYPE"],
                    length: column?["DATA_LENGTH"] as Integer
                    , scale: column?["DATA_SCALE"] as Integer,
                    isNullable: column?["NULLABLE"]?.toString() == "Y",
                    defaultValue: column?["DATA_DEFAULT"])
        }
        return t
    }


    boolean execute(String sql) throws SQLException {
        if(sql?.contains(";")){
            sql.split(";(?=(?:[^']*'[^']*')*[^']*\$)").collect { it.trim()}.findAll { it}.each {
                super.execute(it)
            }
        }
        return true
    }
}
