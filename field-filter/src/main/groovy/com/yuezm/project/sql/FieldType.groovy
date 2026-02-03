package com.yuezm.project.sql


/**
 * FieldType
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2026/2/2 11:47
 */
abstract class FieldType {
    /**
     * 字段名称
     */
    String name

    /**
     * 字段类型
     */
    String type

    /**
     * 长度
     */
    long[] length


    /**
     * 版本
     */
    String version


    /**
     * 转换为可以适配sql字段类型
     */
    abstract void convert(TableInfo tableInfo)


}
