package com.yuezm.project.sql
/**
 * TableField
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/8/15 11:10
 */

class TableField {


    /**
     * 列名称
     */
    String colName
    /**
     * 数据类型
     */
    String dataType


    /**
     * 字段注释
     */
    String comment


    /**
     * 字段长度
     */
    Integer length = 0

    /**
     * 小数位数
     */
    Integer scale = 0


    /**
     * 默认值
     */
    String defaultValue


    /**
     * 是否主键
     */
    boolean isPrimaryKey


    /**
     * 是否可以为null
     */
    boolean isNullable


    /**
     * 是否自增
     */
    boolean autoIncrement


    /**
     * 是否唯一索引
     */
    boolean isUnique


}
