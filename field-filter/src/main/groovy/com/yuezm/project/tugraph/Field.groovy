package com.yuezm.project.tugraph

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Field
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/6/27 13:38
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD])
@interface Field {


    /**
     * 是否作为索引字段
     * @return
     */
    boolean isIndex() default false

    /**
     * 数据是否唯一
     * @return
     */
    boolean unique() default false

    /**
     * 字段类型
     * @return
     */
    String columnType() default ""

    /**
     * 字段名
     * @return
     */
    String columnName() default ""

    /**
     * 是否选填
     * @return
     */
    boolean optional() default false

    /**
     * 是否存在映射
     * @return
     */
    boolean exist() default true

}