package com.yuezm.project.sql.mysql

import com.yuezm.project.sql.Wrapper


/**
 * MysqlWrapper
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/8/1 10:08
 */
class MySqlWrapper extends Wrapper{
    @Override
    String getColumn(String column) {
        return "`${column}`"
    }
}
