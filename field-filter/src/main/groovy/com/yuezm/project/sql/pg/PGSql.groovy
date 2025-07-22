package com.yuezm.project.sql.pg

import com.yuezm.project.sql.SqlHandler

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


}
