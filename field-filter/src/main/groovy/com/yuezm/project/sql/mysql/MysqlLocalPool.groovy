package com.yuezm.project.sql.mysql

import com.yuezm.project.sql.DatasourceProperties
import com.yuezm.project.sql.FieldType
import com.yuezm.project.sql.SqlLocalPoolHandler
import com.yuezm.project.sql.TableField
import com.yuezm.project.sql.TableInfo
import com.yuezm.project.sql.Wrapper


/**
 * MysqlLocalPool
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2026/1/15 9:59
 */
class MysqlLocalPool extends SqlLocalPoolHandler{


    MysqlLocalPool(DatasourceProperties datasourceProperties, Map<String, Object> otherPoolConfig = [:]) {
        super(datasourceProperties, otherPoolConfig)
    }



    @Override
    Number getTableDataCapacity(String tableName, String schema = null) {
        String sql = "SELECT " +
                "    ROUND((data_length + index_length) , 2) AS `total` " +
                "FROM information_schema.TABLES " +
                "WHERE table_schema = '$schema' " +
                "  AND table_name = '$tableName'; "
        return firstRow(sql)?["total"] as Number
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
        return selfWrapper
    }

    @Override
    List<Map<String, Object>> getTablePrimarys(String tableName, String schema = null) {
        String sql ="SELECT\n" +
                "    COLUMN_NAME \n" +
                "FROM information_schema.KEY_COLUMN_USAGE\n" +
                "WHERE TABLE_SCHEMA = '$schema'\n" +
                "  AND TABLE_NAME = '$tableName'\n" +
                "  AND CONSTRAINT_NAME = 'PRIMARY';"

        return rows(sql)
    }

    @Override
    TableInfo getTableInfo(String tableName, String schema = null) {
        String sql = "select TABLE_SCHEMA,TABLE_COMMENT from information_schema.TABLES  where TABLE_NAME = '$tableName' and TABLE_SCHEMA = '$schema'"
        def tableInfo = firstRow(sql)
        if(tableInfo == null){
            return null
        }
        def t = new TableInfo()
        t.tableName = tableInfo?["TABLE_SCHEMA"]
        t.comment = tableInfo?["TABLE_COMMENT"]
        sql = "SELECT \n" +
                "    COLUMN_NAME,\n" +
                "    COLUMN_TYPE,\n" +
                "    IS_NULLABLE,\n" +
                "    COLUMN_KEY,\n" +
                "    COLUMN_DEFAULT,\n" +
                "    EXTRA,\n" +
                "    COLUMN_COMMENT,\n" +
                "    CHARACTER_SET_NAME,\n" +
                "    COLLATION_NAME\n" +
                "FROM information_schema.COLUMNS\n" +
                "WHERE \n" +
                "  TABLE_NAME = '$tableName' "

        def columns = rows(sql)
        def fields = columns?.collect { column ->

            return new TableField(
                    colName: column?["COLUMN_NAME"],
                    dataType: column?["COLUMN_TYPE"],
                    comment: column?["COLUMN_COMMENT"],
                    isNullable: column?["IS_NULLABLE"]?.toString() == "NO",
                    isPrimaryKey: column?["COLUMN_KEY"]?.toString() == "PRI",
            )
        }

        t.fields = fields
        return t
    }

    @Override
    List<FieldType> supportFieldTypes(String type = null, String version = "8.0") {
        return MySqlFieldType.getFieldTypes(type, version)
    }
}
