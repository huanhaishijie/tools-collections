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


}
