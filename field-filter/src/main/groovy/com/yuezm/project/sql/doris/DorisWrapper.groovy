package com.yuezm.project.sql.doris

import com.yuezm.project.sql.TableInfo
import com.yuezm.project.sql.Wrapper


/**
 * MysqlWrapper
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/8/1 10:08
 */
class DorisWrapper extends Wrapper {
    @Override
    String getColumn(String column) {
        return "`${column}`"
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
        return sql + " limit ${offset},${limit}"
    }

    @Override
    <T extends TableInfo> String generateDdl(T t, Closure<T> closure = null) {
        if (closure) {
            return super.generateDdl(t, closure)
        }
        if (t == null) throw new IllegalArgumentException("tableInfo can't be null")
        def ddl = " CREATE TABLE $t.tableName (\n"
        String primary = ""
        List<String> pks = []
        t?.fields?.each { field ->
            super.validColName(field.colName)
            field.with {
                switch (dataType.toLowerCase()) {
                    case "number":
                        dataType = "decimal"
                        break
                    case "clob":
                        dataType = "text"
                        break
                    case "varchar2":
                        dataType = "varchar"
                        break
                    case "nvarchar2":
                        dataType = "varchar"
                        break
                }
                ddl += " ${getColumn(colName)} $dataType"
                if (length && isSupportLength(dataType)) {
                    if (!"CLOB".equalsIgnoreCase(dataType)) {
                        if (("numeric".equalsIgnoreCase(dataType) || "decimal".equalsIgnoreCase(dataType)) && length <= 65) {
                            ddl += "($length"
                            if (scale) {
                                ddl += ",$scale"
                            }
                            ddl += ")"
                        } else {
                            ddl += "($length)"
                        }
                    }
                } else {
                    if ("VARCHAR".equalsIgnoreCase(dataType) || "VARCHAR2".equalsIgnoreCase(dataType)) {
                        ddl = ddl[0.. -(dataType.length() + 1)]
                        ddl += "VARCHAR(510)"
                    }
                }

                if (!isNullable) {
                    ddl += " NOT NULL"
                } else {
                    if (dataType.equalsIgnoreCase("timestamp")) {
//                        ddl += " DEFAULT CURRENT_TIMESTAMP"
                    }
                }

                if (comment) {
                    ddl += " COMMENT '$comment'"
                }
                if (isPrimaryKey) {
                    pks << getColumn(colName)
                }

                if (t.fields[-1] != it) {
                    ddl += ","
                }

            }
        }

        if (pks.size() > 0) {
            ddl += ","
            primary = "PRIMARY KEY (${pks.join(",")})"
        }
        ddl += " $primary )"

        if (t?.comment) {
            ddl += " COMMENT = '${t.comment}'"
        }
        ddl += ";"
        return ddl
    }

    private boolean isSupportLength(String dataType) {
        boolean isSupport = true
        switch (dataType.toLowerCase()) {

        // 整数类型（MySQL 8.0 已废弃显示宽度，不支持 length）
            case "tinyint":
            case "smallint":
            case "mediumint":
            case "int":
            case "integer":
            case "bigint":
                isSupport = false
                break

                // 浮点类型（不支持 length，decimal 只能写精度 p,s，不算长度）
            case "float":
            case "double":
            case "real":
                isSupport = false
                break

                // 日期时间类型（只有 time/timestamp 可写 fsp，其它不支持长度）
            case "date":
            case "datetime":
            case "year":
                isSupport = false
                break

                // 文本/大对象类型
            case "text":
            case "tinytext":
            case "mediumtext":
            case "longtext":
            case "blob":
            case "tinyblob":
            case "mediumblob":
            case "longblob":
                isSupport = false
                break

                // JSON、枚举、集合
            case "json":
            case "enum":
            case "set":
                isSupport = false
                break
        }
        return isSupport
    }



}
