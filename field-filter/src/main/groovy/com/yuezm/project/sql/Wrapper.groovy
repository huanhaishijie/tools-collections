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

    protected SqlBuilder sqlBuilder

    String tableName

    abstract String getColumn(String column)

    abstract String getColumns(String... columns)

    abstract String getColumns(List<String> columns)

    SqlBuilder sqlBuilder(){
        if(sqlBuilder == null){
            sqlBuilder = new SqlBuilder(this)
        }
        return sqlBuilder
    }

    abstract String getTotalCountSql(String sql)

    abstract String getPageSql(String sql , Object offset , Object limit)

    /**
     * 生成ddl
     * @param t
     * @param closure
     * @return
     */
    default <T extends TableInfo> String generateDdl(T t, Closure<T> closure = null){
        if(closure == null){
            return t.toString()
        }
        return closure.call(t)
    }

    /**
     * 校验字段name是否合规
     * @param name
     */
    void validColName(String name){
        if (!name.matches("^[a-zA-Z][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("Invalid column name: ${name}. Must match '^[a-zA-Z][a-zA-Z0-9_]*'");
        }
    }
}
