package com.yuezm.project.common.wordfill

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD, ElementType.METHOD])
@interface FillList {
    int index() default 0

    String type() default "" // "" 默认值， irregular 不规则表格填充
}