package com.yuezm.project.connector


/**
 * SqlParamUtil
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2026/1/4 14:51
 */
class SqlParamUtil {

    static class ParsedSql {
        String sql
        List<Object> params
    }

    static ParsedSql parse(String sql, Map<String, ?> namedParams) {
        List<Object> params = []
        String parsed = sql.replaceAll(/:\w+/) { m ->
            String key = m[1..-1]
            params << namedParams[key]
            return '?'
        }
        return new ParsedSql(sql: parsed, params: params)
    }
}

