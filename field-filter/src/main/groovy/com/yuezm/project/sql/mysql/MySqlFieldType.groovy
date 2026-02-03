package com.yuezm.project.sql.mysql

import com.yuezm.project.sql.FieldType
import com.yuezm.project.sql.TableInfo


/**
 * MySqlFieldType
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2026/2/2 13:59
 */
class MySqlFieldType extends FieldType{
    final static List<MySqlFieldType> types

    static {
        types = [
                /**
                 * version 8.0
                 * link:https://dev.mysql.com/doc/refman/8.0/en/data-types.html
                 */
                //Numeric
                new MySqlFieldType(name: "BIT", type: "Numeric", version: "8.0"),
                new MySqlFieldType(name: "TINYINT", type: "Numeric", version: "8.0"),
                new MySqlFieldType(name: "BOOL", type: "Numeric", version: "8.0"),
                new MySqlFieldType(name: "BOOLEAN", type: "Numeric", version: "8.0"),
                new MySqlFieldType(name: "SMALLINT", type: "Numeric", version: "8.0"),
                new MySqlFieldType(name: "MEDIUMINT", type: "Numeric", version: "8.0"),
                new MySqlFieldType(name: "INT", type: "Numeric", version: "8.0"),
                new MySqlFieldType(name: "INTEGER", type: "Numeric", version: "8.0"),
                new MySqlFieldType(name: "BIGINT", type: "Numeric", version: "8.0"),
                new MySqlFieldType(name: "DECIMAL", type: "Numeric", version: "8.0"),
                new MySqlFieldType(name: "FLOAT", type: "Numeric", version: "8.0"),
                new MySqlFieldType(name: "FLOAT", type: "Numeric", version: "8.0"),
                new MySqlFieldType(name: "DOUBLE", type: "Numeric", version: "8.0"),
                //Datetime
                new MySqlFieldType(name: "DATE", type: "Datetime", version: "8.0"),
                new MySqlFieldType(name: "TIME", type: "Datetime", version: "8.0"),
                new MySqlFieldType(name: "DATETIME", type: "Datetime", version: "8.0"),
                new MySqlFieldType(name: "TIMESTAMP", type: "Datetime", version: "8.0"),
                new MySqlFieldType(name: "YEAR", type: "Datetime", version: "8.0"),
                //String
                new MySqlFieldType(name: "CHAR", type: "String", version: "8.0"),
                new MySqlFieldType(name: "VARCHAR", type: "String", version: "8.0"),
                new MySqlFieldType(name: "BINARY", type: "String", version: "8.0"),
                new MySqlFieldType(name: "VARBINARY", type: "String", version: "8.0"),
                new MySqlFieldType(name: "BLOB", type: "String", version: "8.0"),
                new MySqlFieldType(name: "TEXT", type: "String", version: "8.0"),
                new MySqlFieldType(name: "ENUM", type: "String", version: "8.0"),
                new MySqlFieldType(name: "SET", type: "String", version: "8.0"),
                //SpatialData
                new MySqlFieldType(name: "GEOMETRY", type: "SpatialData", version: "8.0"),
                new MySqlFieldType(name: "POINT", type: "SpatialData", version: "8.0"),
                new MySqlFieldType(name: "LINESTRING", type: "SpatialData", version: "8.0"),
                new MySqlFieldType(name: "POLYGON", type: "SpatialData", version: "8.0"),
                new MySqlFieldType(name: "MULTIPOINT", type: "SpatialData", version: "8.0"),
                new MySqlFieldType(name: "MULTILINESTRING", type: "SpatialData", version: "8.0"),
                new MySqlFieldType(name: "MULTIPOLYGON", type: "SpatialData", version: "8.0"),
                new MySqlFieldType(name: "GEOMETRYCOLLECTION", type: "SpatialData", version: "8.0"),
                //Semi-structured
                new MySqlFieldType(name: "JSON", type: "Semi-structured", version: "8.0"),
        ]
    }


    static List<FieldType> getFieldTypes(String type = null, String version = "8.0") {
        List<FieldType> fieldTypeList = types
        if(type){
            fieldTypeList = fieldTypeList.findAll{
                it.type == type
            }
        }
        if(version){
            fieldTypeList = fieldTypeList.findAll {
                it.version == version
            }
        }
        return fieldTypeList
    }

    /**
     * 转换TableInfo为MySQL格式
     * @param tableInfo 输入的TableInfo，可能来自其他数据库
     */
    @Override
    void convert(TableInfo tableInfo) {
        if (!tableInfo?.fields) {
            return
        }

        tableInfo.fields.each { field ->
            String originalType = field.dataType?.toUpperCase()
            
            // Oracle到MySQL的类型映射
            String mysqlType = convertToMySqlType(originalType)
            
            if (mysqlType) {
                field.dataType = mysqlType
            }
        }
    }

    /**
     * 将其他数据库类型转换为MySQL类型
     */
    private String convertToMySqlType(String originalType) {
        if (!originalType) return originalType

        // Oracle类型映射
        switch (originalType) {
            case "NUMBER":
                return "DECIMAL"
            case "VARCHAR2":
                return "VARCHAR"
            case "NVARCHAR2":
                return "VARCHAR"
            case "NCHAR":
                return "CHAR"
            case "CLOB":
                return "TEXT"
            case "NCLOB":
                return "TEXT"
            case "BINARY_FLOAT":
                return "FLOAT"
            case "BINARY_DOUBLE":
                return "DOUBLE"
            case "RAW":
                return "BINARY"
            case "LONG RAW":
                return "BLOB"
        }

        // PostgreSQL类型映射
        switch (originalType) {
            case "INTEGER":
            case "BIGINT":
            case "SMALLINT":
                return originalType.toUpperCase()
            case "SERIAL":
                return "INT"
            case "BIGSERIAL":
                return "BIGINT"
            case "SMALLSERIAL":
                return "SMALLINT"
            case "CHARACTER VARYING":
            case "VARCHAR":
                return "VARCHAR"
            case "CHARACTER":
            case "CHAR":
                return "CHAR"
            case "TEXT":
                return "TEXT"
            case "BYTEA":
                return "BLOB"
            case "BOOLEAN":
                return "BOOLEAN"
            case "REAL":
                return "FLOAT"
            case "DOUBLE PRECISION":
                return "DOUBLE"
            case "TIMESTAMP":
            case "TIMESTAMPTZ":
                return "TIMESTAMP"
            case "DATE":
                return "DATE"
            case "TIME":
            case "TIMETZ":
                return "TIME"
            case "JSON":
            case "JSONB":
                return "JSON"
        }

        // Doris类型映射（大部分与MySQL兼容）
        switch (originalType) {
            case "LARGEINT":
                return "BIGINT" // MySQL没有LARGEINT，用BIGINT代替
            case "VARIANT":
                return "JSON" // MySQL没有VARIANT，用JSON代替
            case "HLL":
            case "BITMAP":
            case "QUANTILE_STATE":
            case "AGG_STATE":
                return "VARCHAR" // 聚合类型用VARCHAR存储
            case "IPv4":
            case "IPv6":
                return "VARCHAR" // IP类型用VARCHAR存储
        }

        // 如果没有找到映射，返回原类型
        return originalType
    }

}
