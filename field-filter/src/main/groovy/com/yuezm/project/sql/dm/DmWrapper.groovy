package com.yuezm.project.sql.dm


import com.yuezm.project.sql.TableInfo
import com.yuezm.project.sql.Wrapper

/**
 * DmWrapper
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/8/1 10:06
 */
class DmWrapper extends Wrapper {
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
        return "select count(*) from (${sql}) as t "
    }


    @Override
    String getPageSql(String sql, Object offset, Object limit) {
        return sql += " OFFSET $offset ROWS FETCH NEXT $limit ROWS ONLY "
    }

    @Override
    <T extends TableInfo> String generateDdl(T t, Closure<T> closure = null) {
        if (closure) {
            return super.generateDdl(t, closure)
        }
        if (t == null) throw new IllegalArgumentException("tableInfo can't be null")
        def ddl = " CREATE TABLE $t.tableName (\n"
        String primary = ""
        List<String> comments = []
        t?.fields?.each {
            super.validColName(it.colName)
            def colName = getColumn(it.colName)
            it.with {self ->
                if(dataType.equalsIgnoreCase("int2") || dataType.equalsIgnoreCase("int4")||dataType.equalsIgnoreCase("int8")){
                    dataType="int"
                }else if(dataType.equalsIgnoreCase("mediumtext")) {
                    dataType="clob"
                }
            }

            ddl += " $colName $it.dataType"
            if (it.length && isSupportLength(it.dataType)) {
                if(!"CLOB".equalsIgnoreCase(it.dataType)
                        && !"BLOB".equalsIgnoreCase(it.dataType)
                        && !"mediumtext".equalsIgnoreCase(it.dataType)
                        && !"text".equalsIgnoreCase(it.dataType)
                        && !"int".equalsIgnoreCase(it.dataType)
                        && !"int2".equalsIgnoreCase(it.dataType)
                        && !"int4".equalsIgnoreCase(it.dataType)
                        && !"int8".equalsIgnoreCase(it.dataType)
                        && !"date".equalsIgnoreCase(it.dataType)
                ){
                    if("numeric".equalsIgnoreCase(it.dataType) && it.length<=38){
                        ddl += "($it.length"
                        if(it.scale){
                            ddl += ",$it.scale"
                        }
                        ddl += ")"
                    }else if(!"numeric".equalsIgnoreCase(it.dataType)){
                        ddl += "($it.length)"
                    }
                }

            }else {
                if("CHAR".equalsIgnoreCase(it.dataType) || "NCHAR".equalsIgnoreCase(it.dataType)){
                    ddl = ddl[0.. -(it.dataType.length() + 1)]
                    ddl += "VARCHAR(4000)"
                }else if("VARCHAR".equalsIgnoreCase(it.dataType) || "VARCHAR2".equalsIgnoreCase(it.dataType)){
                    ddl = ddl[0.. -(it.dataType.length() + 1)]
                    ddl += "VARCHAR(4000)"
                }else if("NVARCHAR".equalsIgnoreCase(it.dataType) || "NVARCHAR2".equalsIgnoreCase(it.dataType)){
                    ddl = ddl[0.. -(it.dataType.length() + 1)]
                    ddl += "VARCHAR(2000)"
                }
            }
            if (!it.isNullable) {
                ddl += " NOT NULL"
            }
            if(t?.fields[-1] != it){
                ddl += ","
            }
            if (it.isPrimaryKey) {
                t?.fields
                primary = "PRIMARY KEY ($colName)"
            }
            if (it.comment) {
                comments << "COMMENT ON COLUMN $t.tableName.$colName IS '${it.comment}';"
            }
        }
        if (t.comment) {
            comments << " COMMENT ON TABLE $t.tableName IS '${t.comment}';"
        }
        if(primary){
            ddl += ","
        }
        ddl += "$primary);"
        if (comments.size() > 0) {
            comments.each {
                ddl += "\n$it"
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


