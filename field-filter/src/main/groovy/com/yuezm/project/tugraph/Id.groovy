package com.yuezm.project.tugraph

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Id
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/6/27 13:37
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD])
@interface Id {

}