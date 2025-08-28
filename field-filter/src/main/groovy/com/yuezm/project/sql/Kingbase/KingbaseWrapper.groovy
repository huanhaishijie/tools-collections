package com.yuezm.project.sql.Kingbase

import com.yuezm.project.sql.TableInfo
import com.yuezm.project.sql.Wrapper


/**
 * KingbaseWrapper
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/8/1 10:07
 */
class KingbaseWrapper extends Wrapper{
    @Override
    String getColumn(String column) {
        return "\"${column}\""
    }

    @Override
    String getColumns(String... columns) {
        return columns.collect { getColumn(it) }.join(",")
    }

    @Override
    String getColumns(List<String> columns) {
        return columns.collect { getColumn(it) }.join(",")
    }

    @Override
    String getTotalCountSql(String sql) {
        return "select count(t.*) from (${sql}) as t "
    }

    @Override
    String getPageSql(String sql, Object offset, Object limit) {
        return sql + " limit ${limit} offset ${offset}"
    }


    @Override
    <T extends TableInfo> String generateDdl(T t, Closure<T> closure = null) {
        if(closure){
            return super.generateDdl(t, closure)
        }
        def ddl = " CREATE TABLE $t.tableName (\n"
        def pks= []
        def primary = ""
        def comments = []
        t?.fields?.each { f ->
            super.validColName(f.colName)
            f.with {
                ddl += " ${getColumn(colName)} "
                switch (dataType.toLowerCase()){
                    case "number":
                        dataType = "decimal"
                        break
                    case "clob":
                    case "mediumtext":
                        dataType = "text"
                        break
                    case "varchar2":
                    case "nvarchar2":
                        dataType = "varchar"
                        break
                    case "blob":
                        dataType = "bytea"
                        break
                    case "geometry":
                        dataType = "public.geometry"
                        break
                }
                ddl += " $dataType"
                if(length  && isSupportLength(dataType)){
                    if(dataType.equalsIgnoreCase("numeric") || dataType.equalsIgnoreCase("decimal")){
                        ddl += "($length"
                        if(scale){
                            ddl += ",$scale"
                        }
                        ddl += ")"
                    }else{
                        ddl += "($length)"
                    }
                }

                if(!isNullable){
                    ddl += " NOT NULL "
                }
                if(t?.fields[-1] != it){
                    ddl += ","
                }

                if(isPrimaryKey){
                    pks << getColumn(colName)
                }
                if(comment){
                    comments << " COMMENT ON COLUMN $t.tableName.${getColumn(colName)} IS '${comment}' ;"
                }
            }
        }
        if(pks.size() > 0){
            ddl += ","
            primary = "PRIMARY KEY (${pks.join(",")})"
        }
        ddl += "$primary) ;"
        if(t.comment){
            comments << " COMMENT ON TABLE $t.tableName IS '${t.comment}' ;"
        }
        if(comments.size() > 0){
            comments.each {
                ddl += "\n$it"
            }
        }
        return ddl
    }


    private boolean isSupportLength(String dataType){
        boolean isSupport = true
        switch (dataType.toLowerCase()){
            case "int2":
            case "int4":
            case "int8":
            case "float4":
            case "float8":
            case "date":
            case "time":
            case "timestamp":
            case "timestamptz":
            case "text":
                isSupport = false
                break
        }
        return isSupport
    }


}
