package com.yuezm.project.sql.oracle

import com.yuezm.project.sql.Wrapper


/**
 * OracleSqlWrapper
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/8/1 10:09
 */
class OracleSqlWrapper extends Wrapper{
    @Override
    String getColumn(String column) {
        return "\"${column}\""
    }
}
