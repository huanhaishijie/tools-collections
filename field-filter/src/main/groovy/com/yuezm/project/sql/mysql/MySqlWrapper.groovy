package com.yuezm.project.sql.mysql

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
class MySqlWrapper extends Wrapper {
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
    String getTotalCountSql(String sql) {
        return "select count(t.*) from (${sql}) as t "
    }

    @Override
    String getPageSql(String sql, Object offset, Object limit) {
        return sql + " limit ${limit},${offset}"
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
                ddl += " ${getColumn(colName)} $dataType "
                if (length) {
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
                        ddl += "VARCHAR(510)"
                    }
                }

                if (!isNullable) {
                    ddl += " NOT NULL"
                } else {
                    if (dataType.equalsIgnoreCase("timestamp")) {
                        ddl += " DEFAULT CURRENT_TIMESTAMP"
                    }
                }

                if (isPrimaryKey) {
                    pks << getColumn(colName)
                }

                if (comment) {
                    ddl += " COMMENT  ON '$comment'"
                }
                ddl += ","
            }
        }

        if (pks.size() > 0) {
            primary = "PRIMARY KEY (${pks.join(",")})"
        }
        ddl += " $primary )"

        if (t?.comment) {
            ddl += " COMMENT = '${t.comment}'"
        }
        ddl += ";"
        return ddl
    }



}
