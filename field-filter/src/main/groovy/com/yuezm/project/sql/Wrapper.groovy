package com.yuezm.project.sql


/**
 * Wrapper
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/7/31 15:28
 */
abstract class Wrapper {





    String tableName

    abstract String getColumn(String column)

    abstract String getColumns(String... columns)

    abstract String getColumns(List<String> columns)

    SqlBuilder sqlBuilder(){
        new SqlBuilder(this)
    }




}
