package com.yuezm.project.sql.pg

import com.yuezm.project.sql.SqlHandler
import com.yuezm.project.sql.Wrapper
import com.yuezm.project.sql.dm.DmWrapper

import java.sql.Connection


/**
 * PGSql
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/6/20 9:46
 */
class PGSql extends SqlHandler {

    PGSql(Connection connection) {
        super(connection)
    }
//try {
    //    // 获取数据库类型
    //    String databaseType = datasource.getCode(); // 假设code字段存储了数据库类型
    //
    //    // 根据数据库类型构建对应的SQL
    //    String sql = "";
    //    switch (databaseType.toLowerCase()) {
    //        case "postgresql":
    //            sql = "SELECT COUNT(*) FROM pg_extension WHERE extname = 'postgis'";
    //            break;
    //        case "oracle":
    //            sql = "SELECT COUNT(*) FROM all_objects WHERE object_name = 'SDO_GEOMETRY'";
    //            break;
    //        case "mysql":
    //            sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'geometry_columns'";
    //            break;
    //        case "dm": // 达梦
    //            sql = "SELECT COUNT(*) FROM sys_synonym WHERE synonym_name = 'GEOMETRY'";
    //            break;
    //        case "kingbase": // 人大金仓
    //            sql = "SELECT COUNT(*) FROM pg_extension WHERE extname = 'postgis'";
    //            break;
    //        default:
    //            logger.info("不支持的数据库类型---{}, type: {}", shpFileTaskDto.getId(), databaseType);
    //            datasourceShpfileService.updateState(shpFileTaskDto.getId(), "3");
    //            return TaskState.success;
    //    }
    //
    //    // 执行SQL查询
    //    int count = sqlHandler.queryForInt(sql);
    //    if (count == 0) {
    //        logger.info("数据库未启用GIS扩展---{}, type: {}", shpFileTaskDto.getId(), databaseType);
    //        datasourceShpfileService.updateState(shpFileTaskDto.getId(), "3");
    //        return TaskState.success;
    //    }
    //} catch (Exception e) {
    //    logger.info("检查GIS扩展失败---{}, error: {}", shpFileTaskDto.getId(), e.getMessage());
    //    datasourceShpfileService.updateState(shpFileTaskDto.getId(), "3");
    //    return TaskState.success;
    //}
    boolean isSupportGis() {
        String query = "SELECT COUNT(*) count FROM pg_extension WHERE extname = 'postgis'"
        long count = sql.firstRow(query).count as long
        return count > 0
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
            selfWrapper = new PGSqlWrapper()
        }
        return selfWrapper
    }

    @Override
    Number getTableDataCapacity(String tableName, String schema = null) {
        String sql = "SELECT pg_total_relation_size('$tableName') AS total_size"
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
                "  AND tc.table_schema = '$schema';"
        return rows(sql)
    }







}
