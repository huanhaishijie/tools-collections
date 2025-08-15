package com.yuezm.project.sql


/**
 * TableInfo
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/8/15 11:20
 */
class TableInfo {

    /**
     * 表名称
     */
    String tableName

    /**
     * 表注释
     */
    String comment

    /**
     * 表字段
     */
    List<? extends TableField> fields


    void addAll(List<TableField> fields){
        if(fields){
            this.fields.addAll fields
        }
    }

    void addAll(TableField ... fields){
        if(fields){
            this.fields.addAll fields
        }
    }

}
