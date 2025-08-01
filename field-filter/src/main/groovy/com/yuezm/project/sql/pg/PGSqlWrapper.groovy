package com.yuezm.project.sql.pg

import com.yuezm.project.sql.Wrapper


/**
 * PGSqlWrapper
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/8/1 10:13
 */
class PGSqlWrapper extends Wrapper{
    @Override
    String getColumn(String column) {
        return "\"${column}\""
    }
}
