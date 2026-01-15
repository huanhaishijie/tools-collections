package com.yuezm.project.sql.Kingbase

import com.yuezm.project.sql.DatasourceProperties
import com.yuezm.project.sql.SqlLocalPoolHandler
import com.yuezm.project.sql.TableField
import com.yuezm.project.sql.TableInfo
import com.yuezm.project.sql.Wrapper


/**
 * KingbaseSqlLocalPool
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2026/1/15 10:01
 */
class KingbaseSqlLocalPool extends SqlLocalPoolHandler{

    KingbaseSqlLocalPool(DatasourceProperties datasourceProperties, Map<String, Object> otherPoolConfig = [:]) {
        super(datasourceProperties, otherPoolConfig)
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
        if(selfWrapper == null){
            selfWrapper = new KingbaseWrapper()
        }
        return selfWrapper
    }

    @Override
    Number getTableDataCapacity(String tableName, String schema = null) {
        String sql = "SELECT pg_total_relation_size('$tableName') AS total_size".toString()
        return firstRow(sql)?["total_size"] as Number
    }


    @Override
    List<Map<String, Object>> getTablePrimarys(String tableName, String schema = null) {
        String sql = "SELECT kcu.column_name \"COLUMN_NAME\" " +
                "FROM information_schema.table_constraints tc\n" +
                "JOIN information_schema.key_column_usage kcu\n" +
                "  ON tc.constraint_name = kcu.constraint_name\n" +
                "  AND tc.table_schema = kcu.table_schema\n" +
                "WHERE tc.constraint_type = 'PRIMARY KEY'\n" +
                "  AND tc.table_name = '$tableName'\n" +
                "  AND tc.table_schema = '$schema';".toString()
        return rows(sql)
    }



    @Override
    TableInfo getTableInfo(String tableName, String schema = null) {
        def info = new TableInfo()
        String sql = "SELECT\n" +
                "    obj_description(relfilenode, 'pg_class') AS table_comment\n" +
                "FROM pg_class\n" +
                "WHERE relname = '$tableName'\n" +
                "  AND relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = '$schema');"
        def row = firstRow(sql)
        if(row){
            info.comment = row?["table_comment"]
        }
        sql = "SELECT\n" +
                "    c.ordinal_position AS column_id,\n" +
                "    c.column_name,\n" +
                "    c.data_type,\n" +
                "    c.character_maximum_length,\n" +
                "    c.numeric_precision,\n" +
                "    c.numeric_scale,\n" +
                "    c.is_nullable,\n" +
                "    c.column_default,\n" +
                "    d.description AS column_comment,\n" +
                "    CASE WHEN kcu.column_name IS NOT NULL THEN 'YES' ELSE 'NO' END AS is_primary_key\n" +
                "FROM information_schema.columns c\n" +
                "LEFT JOIN pg_catalog.pg_description d\n" +
                "       ON d.objoid = (SELECT oid FROM pg_class WHERE relname = c.table_name\n" +
                "                      AND relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = c.table_schema))\n" +
                "      AND d.objsubid = c.ordinal_position\n" +
                "LEFT JOIN information_schema.key_column_usage kcu\n" +
                "       ON c.table_name = kcu.table_name\n" +
                "      AND c.table_schema = kcu.table_schema\n" +
                "      AND c.column_name = kcu.column_name\n" +
                "      AND kcu.constraint_name IN (\n" +
                "            SELECT constraint_name\n" +
                "            FROM information_schema.table_constraints\n" +
                "            WHERE table_name = c.table_name\n" +
                "              AND table_schema = c.table_schema\n" +
                "              AND constraint_type = 'PRIMARY KEY'\n" +
                "       )\n" +
                "WHERE c.table_name = '$tableName'\n" +
                "  AND c.table_schema = '$schema'\n" +
                "ORDER BY c.ordinal_position;".toString()
        def results = rows(sql)
        info.fields = results.collect {
            return new TableField(
                    colName: it["column_name"],
                    dataType: it["data_type"],
                    comment: it["column_comment"],
                    isNullable: it["is_nullable"] == "YES",
                    defaultValue: it["column_default"],
                    scale: it["numeric_scale"] as Integer,
                    length: it["character_maximum_length"] as Integer,
                    isPrimaryKey: it["is_primary_key"] == "YES"
            )
        }
        return info
    }
}
