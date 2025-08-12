package com.yuezm.project.sql.Kingbase

import com.yuezm.project.sql.Wrapper


/**
 * KingbaseWrapper
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/8/1 10:07
 */
class KingbaseWrapper extends Wrapper{
    @Override
    String getColumn(String column) {
        return "\"${column}\""
    }

    @Override
    String getColumns(String... columns) {
        return columns.collect { getColumn(it) }.join(",")
    }

    @Override
    String getColumns(List<String> columns) {
        return columns.collect { getColumn(it) }.join(",")
    }
}
