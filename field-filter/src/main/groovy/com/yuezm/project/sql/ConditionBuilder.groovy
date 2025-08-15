package com.yuezm.project.sql

/**
 * CondtionBuilder
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/8/12 10:35
 */
@FunctionalInterface
interface ConditionBuilder<T> {

    String buildCondition(Map<String, Object> conditions, String key, T t)

}