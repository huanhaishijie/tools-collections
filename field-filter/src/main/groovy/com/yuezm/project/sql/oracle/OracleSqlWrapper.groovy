package com.yuezm.project.sql.oracle

import com.yuezm.project.sql.TableInfo
import com.yuezm.project.sql.Wrapper


/**
 * OracleSqlWrapper
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/8/1 10:09
 */
class OracleSqlWrapper extends Wrapper{
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
    String getTotalCountSql(String sql, Boolean isSingleFrom = false) {
        if(isSingleFrom){
            if(sql.contains("SELECT")){
                sql = sql.replace("SELECT", "select")
            }
            if(sql.contains("FROM")){
                sql = sql.replace("FROM", "from")
            }
            //将sql select 到 from 替换为count(*)
            if(sql.contains("select ") && sql.contains(" from")) {
                def fromIndex = sql.indexOf(" from")
                def selectIndex = sql.indexOf("select ") + "select ".length()
                sql = sql.substring(0, selectIndex) + " count(*) " + sql.substring(fromIndex)
            }
            if(sql.contains("ORDER BY")){
                sql = sql.replace("ORDER BY", "order by")
            }
            if(sql.contains("order BY")){
                sql = sql.replace("order BY", "order by")
            }
            if(sql.contains("ORDER by")){
                sql = sql.replace("ORDER by", "order by")
            }
            if(sql.contains("order by")){
                //将sql 从order by 后面开始丢弃
                def orderIndex = sql.indexOf("order by")
                sql = sql.substring(0, orderIndex)
            }
            return sql
        }
        return "select count(*) from (${sql})  t "
    }


    @Override
    String getPageSql(String sql, Object offset, Object limit) {
        return sql += " OFFSET $offset ROWS FETCH NEXT $limit ROWS ONLY "
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
        t.fields.each { f ->
            super.validColName(f.colName)
            f.with {
                ddl += " ${getColumn(colName)} "

                switch (dataType.toLowerCase()) {
                    case "int2":
                    case "int4":
                    case "int8":
                        dataType = "int"
                        break
                    case "mediumtext":
                    case "text":
                            dataType = "clob"
                            break
                }
                ddl += " $dataType"
                if (length && isSupportLength(dataType)) {

                    if(!"CLOB".equalsIgnoreCase(dataType)
                            && !"BLOB".equalsIgnoreCase(dataType)
                            && !"mediumtext".equalsIgnoreCase(dataType)
                            && !"text".equalsIgnoreCase(dataType)
                            && !"int".equalsIgnoreCase(dataType)
                            && !"int2".equalsIgnoreCase(dataType)
                            && !"int4".equalsIgnoreCase(dataType)
                            && !"int8".equalsIgnoreCase(dataType)
                            && !"date".equalsIgnoreCase(dataType)
                    ){
                        if("numeric".equalsIgnoreCase(dataType) && length<=38){
                            ddl += "($length"
                            if(scale){
                                ddl += ",$scale"
                            }
                            ddl += ")"
                        }else if(!"numeric".equalsIgnoreCase(dataType)){
                            ddl += "($length)"
                        }
                    }


                }else {
                    if("CHAR".equalsIgnoreCase(dataType) || "NCHAR".equalsIgnoreCase(dataType)){
                        ddl = ddl[0.. -(dataType.length() + 1)]
                        ddl += "VARCHAR(4000)"
                    }else if("VARCHAR".equalsIgnoreCase(dataType) || "VARCHAR2".equalsIgnoreCase(dataType)){
                        ddl = ddl[0.. -(dataType.length() + 1)]
                        ddl += "VARCHAR(4000)"
                    }else if("NVARCHAR".equalsIgnoreCase(dataType) || "NVARCHAR2".equalsIgnoreCase(dataType)){
                        ddl = ddl[0.. -(dataType.length() + 1)]
                        ddl += "VARCHAR(2000)"
                    }
                }

                if (!isNullable) {
                    ddl += " NOT NULL "
                }
                if(t.fields[-1] != it){
                    ddl += ","
                }

                if (isPrimaryKey) {
                    pks << getColumn(colName)
                }

                if(comment){
                    comments << " COMMENT ON COLUMN $t.tableName.${getColumn(colName)} IS '${comment}';"
                }
            }
        }

        if(pks.size() > 0){
            ddl += ","
            primary = "PRIMARY KEY (${pks.join(",")})"
        }
        ddl += "$primary);"
        if(t.comment){
            comments << " COMMENT ON TABLE $t.tableName IS '${t.comment}';"
        }
        if(comments.size() > 0){
            comments.each {
                ddl += "$it"
            }
        }
        return ddl
    }


    private boolean isSupportLength(String dataType){
        boolean isSupport = true
        switch (dataType.toLowerCase()){
            case "number":
            case "binary_float":
            case "binary_double":
            case "clob":
            case "nclob":
            case "date":
            case "long":
            case "blob":
            case "bfile":
            case "text":
                isSupport = false
                break
        }
        return isSupport
    }
}
