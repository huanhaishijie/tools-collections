package com.yuezm.project.sql.doris

import com.yuezm.project.sql.FieldType
import com.yuezm.project.sql.TableInfo


/**
 * DorisFieldType
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2026/2/2 12:00
 */
class DorisFieldType extends FieldType{

    static List<DorisFieldType> types

    static {
        types = [
                //
                /**
                 * version 3.x
                 * link: https://doris.apache.org/docs/3.x/table-design/data-type
                 */
                //Numeric
                new DorisFieldType(name: "BOOLEAN", type: "Numeric", version: "3.x"),
                new DorisFieldType(name: "TINYINT", type: "Numeric", version: "3.x"),
                new DorisFieldType(name: "SMALLINT", type: "Numeric", version: "3.x"),
                new DorisFieldType(name: "INT", type: "Numeric", version: "3.x"),
                new DorisFieldType(name: "BIGINT", type: "Numeric", version: "3.x"),
                new DorisFieldType(name: "LARGEINT", type: "Numeric", version: "3.x"),
                new DorisFieldType(name: "FLOAT", type: "Numeric", version: "3.x"),
                new DorisFieldType(name: "DOUBLE", type: "Numeric", version: "3.x"),
                new DorisFieldType(name: "DECIMAL", type: "Numeric", version: "3.x"),
                //Datetime
                new DorisFieldType(name: "DATE", type: "Datetime", version: "3.x"),
                new DorisFieldType(name: "DATETIME", type: "Datetime", version: "3.x"),
                //String
                new DorisFieldType(name: "CHAR", type: "String", version: "3.x"),
                new DorisFieldType(name: "VARCHAR", type: "String", version: "3.x"),
                //Semi-structured
                new DorisFieldType(name: "ARRAY", type: "Semi-structured", version: "3.x"),
                new DorisFieldType(name: "MAP", type: "Semi-structured", version: "3.x"),
                new DorisFieldType(name: "STRUCT", type: "Semi-structured", version: "3.x"),
                new DorisFieldType(name: "JSON", type: "Semi-structured", version: "3.x"),
                new DorisFieldType(name: "VARIANT", type: "Semi-structured", version: "3.x"),
                //Aggregation
                new DorisFieldType(name: "HLL", type: "Aggregation", version: "3.x"),
                new DorisFieldType(name: "BITMAP", type: "Aggregation", version: "3.x"),
                new DorisFieldType(name: "QUANTILE_STATE", type: "Aggregation", version: "3.x"),
                new DorisFieldType(name: "AGG_STATE", type: "Aggregation", version: "3.x"),
                //IP
                new DorisFieldType(name: "IPv4", type: "IP", version: "3.x"),
                new DorisFieldType(name: "IPv6", type: "IP", version: "3.x"),

        ]
    }

    static List<FieldType> getFieldTypes(String type = null, String version = "3.x") {
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
     * 转换TableInfo为Doris格式
     * @param tableInfo 输入的TableInfo，可能来自其他数据库
     */
    @Override
    void convert(TableInfo tableInfo) {
        if (!tableInfo?.fields) {
            return
        }

        tableInfo.fields.each { field ->
            String originalType = field.dataType?.toUpperCase()
            
            // 转换为Doris类型
            String dorisType = convertToDorisType(originalType)
            
            if (dorisType) {
                field.dataType = dorisType
            }
        }
    }

    /**
     * 将其他数据库类型转换为Doris类型
     */
    private String convertToDorisType(String originalType) {
        if (!originalType) return originalType

        // MySQL类型映射（大部分兼容）
        switch (originalType) {
            case "TINYINT":
                return "TINYINT"
            case "SMALLINT":
                return "SMALLINT"
            case "MEDIUMINT":
                return "INT" // Doris没有MEDIUMINT，用INT代替
            case "INT":
            case "INTEGER":
                return "INT"
            case "BIGINT":
                return "BIGINT"
            case "FLOAT":
                return "FLOAT"
            case "DOUBLE":
                return "DOUBLE"
            case "DECIMAL":
                return "DECIMAL"
            case "CHAR":
                return "CHAR"
            case "VARCHAR":
                return "VARCHAR"
            case "BINARY":
                return "VARCHAR" // Doris没有BINARY，用VARCHAR代替
            case "VARBINARY":
                return "VARCHAR"
            case "BLOB":
                return "VARCHAR" // Doris没有BLOB，用VARCHAR代替
            case "TEXT":
                return "VARCHAR" // Doris没有TEXT，用VARCHAR代替
            case "DATE":
                return "DATE"
            case "DATETIME":
                return "DATETIME"
            case "TIMESTAMP":
                return "DATETIME" // Doris没有TIMESTAMP，用DATETIME代替
            case "TIME":
                return "VARCHAR" // Doris没有TIME，用VARCHAR代替
            case "YEAR":
                return "INT" // YEAR类型用INT代替
            case "JSON":
                return "JSON"
            case "ENUM":
            case "SET":
                return "VARCHAR" // 枚举和集合类型用VARCHAR代替
        }

        // Oracle类型映射
        switch (originalType) {
            case "NUMBER":
                return "DECIMAL"
            case "FLOAT":
                return "FLOAT"
            case "BINARY_FLOAT":
                return "FLOAT"
            case "BINARY_DOUBLE":
                return "DOUBLE"
            case "CHAR":
                return "CHAR"
            case "VARCHAR2":
                return "VARCHAR"
            case "NCHAR":
                return "CHAR"
            case "NVARCHAR2":
                return "VARCHAR"
            case "CLOB":
                return "VARCHAR"
            case "NCLOB":
                return "VARCHAR"
            case "BLOB":
                return "VARCHAR"
            case "RAW":
                return "VARCHAR"
            case "LONG RAW":
                return "VARCHAR"
            case "DATE":
                return "DATE"
            case "TIMESTAMP":
                return "DATETIME"
            case "TIMESTAMP WITH TIME ZONE":
                return "DATETIME"
            case "TIMESTAMP WITH LOCAL TIME ZONE":
                return "DATETIME"
            case "INTERVAL YEAR TO MONTH":
                return "VARCHAR" // Doris没有INTERVAL类型
            case "INTERVAL DAY TO SECOND":
                return "VARCHAR"
            case "XMLTYPE":
                return "JSON" // XML类型用JSON代替
            case "SDO_GEOMETRY":
                return "VARCHAR" // 空间类型用VARCHAR代替
        }

        // PostgreSQL类型映射
        switch (originalType) {
            case "SMALLINT":
                return "SMALLINT"
            case "INTEGER":
                return "INT"
            case "BIGINT":
                return "BIGINT"
            case "DECIMAL":
            case "NUMERIC":
                return "DECIMAL"
            case "REAL":
                return "FLOAT"
            case "DOUBLE PRECISION":
                return "DOUBLE"
            case "SMALLSERIAL":
                return "SMALLINT"
            case "SERIAL":
                return "INT"
            case "BIGSERIAL":
                return "BIGINT"
            case "CHARACTER VARYING":
            case "VARCHAR":
                return "VARCHAR"
            case "CHARACTER":
            case "CHAR":
                return "CHAR"
            case "TEXT":
                return "VARCHAR"
            case "BYTEA":
                return "VARCHAR"
            case "BOOLEAN":
                return "BOOLEAN"
            case "DATE":
                return "DATE"
            case "TIME":
            case "TIMETZ":
                return "VARCHAR" // Doris没有TIME类型
            case "TIMESTAMP":
            case "TIMESTAMPTZ":
                return "DATETIME"
            case "INTERVAL":
                return "VARCHAR" // Doris没有INTERVAL类型
            case "JSON":
            case "JSONB":
                return "JSON"
            case "UUID":
                return "VARCHAR" // UUID用VARCHAR存储
            case "XML":
                return "JSON" // XML用JSON代替
            case "POINT":
            case "GEOMETRY":
                return "VARCHAR" // 空间类型用VARCHAR代替
            case "INET":
            case "CIDR":
                return "IPv4" // IP类型用IPv4存储
            case "MACADDR":
            case "MACADDR8":
                return "VARCHAR" // MAC地址用VARCHAR存储
        }

        // 如果没有找到映射，返回原类型
        return originalType
    }


}
