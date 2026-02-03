package com.yuezm.project.sql.pg

import com.yuezm.project.sql.FieldType
import com.yuezm.project.sql.TableInfo


/**
 * PGFieldType
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2026/2/2 15:01
 */
class PGFieldType extends FieldType{

    final static List<PGFieldType> types

    static {
        types = [
                /**
                 * version 15
                 * link: https://www.postgresql.org/docs/15/datatype.html
                 */
                //Numeric
                new PGFieldType(name: "smallint", type: "Numeric", version: "15"),
                new PGFieldType(name: "integer", type: "Numeric", version: "15"),
                new PGFieldType(name: "bigint", type: "Numeric", version: "15"),
                new PGFieldType(name: "decimal", type: "Numeric", version: "15"),
                new PGFieldType(name: "numeric", type: "Numeric", version: "15"),
                new PGFieldType(name: "real", type: "Numeric", version: "15"),
                new PGFieldType(name: "double precision", type: "Numeric", version: "15"),
                new PGFieldType(name: "smallserial", type: "Numeric", version: "15"),
                new PGFieldType(name: "serial", type: "Numeric", version: "15"),
                new PGFieldType(name: "bigserial", type: "Numeric", version: "15"),
                //money
                new PGFieldType(name: "money", type: "money", version: "15"),
                //Character
                new PGFieldType(name: "character varying", type: "Character", version: "15"),
                new PGFieldType(name: "character", type: "Character", version: "15"),
                new PGFieldType(name: "char", type: "Character", version: "15"),
                new PGFieldType(name: "varying", type: "Character", version: "15"),
                new PGFieldType(name: "varchar", type: "Character", version: "15"),
                new PGFieldType(name: "text", type: "Character", version: "15"),
                //BinaryData
                new PGFieldType(name: "bytea", type: "BinaryData", version: "15"),
                //Datetime
                new PGFieldType(name: "timestamp", type: "BinaryData", version: "15"),
                new PGFieldType(name: "date", type: "BinaryData", version: "15"),
                new PGFieldType(name: "time", type: "BinaryData", version: "15"),
                new PGFieldType(name: "interval", type: "BinaryData", version: "15"),
                new PGFieldType(name: "timestamptz", type: "BinaryData", version: "15"),
                new PGFieldType(name: "timetz", type: "BinaryData", version: "15"),
                //Boolean Type
                new PGFieldType(name: "boolean", type: "BinaryData", version: "15"),
                //ENUM
                new PGFieldType(name: "ENUM", type: "ENUM", version: "15"),
                //SpatialData
                new PGFieldType(name: "point", type: "SpatialData", version: "15"),
                new PGFieldType(name: "line", type: "SpatialData", version: "15"),
                new PGFieldType(name: "lseg", type: "SpatialData", version: "15"),
                new PGFieldType(name: "box", type: "SpatialData", version: "15"),
                new PGFieldType(name: "path", type: "SpatialData", version: "15"),
                new PGFieldType(name: "polygon", type: "SpatialData", version: "15"),
                new PGFieldType(name: "circle", type: "SpatialData", version: "15"),
                new PGFieldType(name: "geometry", type: "SpatialData", version: "15"),
                //IP
                new PGFieldType(name: "inet", type: "SpatialData", version: "15"),
                new PGFieldType(name: "cidr", type: "SpatialData", version: "15"),
                new PGFieldType(name: "macaddr", type: "SpatialData", version: "15"),
                new PGFieldType(name: "macaddr8", type: "SpatialData", version: "15"),
                //BIT
                new PGFieldType(name: "bit", type: "BIT", version: "15"),
                new PGFieldType(name: "bit varying", type: "BIT", version: "15"),
                new PGFieldType(name: "varbit", type: "BIT", version: "15"),
                //Text Search
                new PGFieldType(name: "tsvector", type: "TextSearch", version: "15"),
                new PGFieldType(name: "tsquery", type: "TextSearch", version: "15"),
                //UUID
                new PGFieldType(name: "uuid", type: "UUID", version: "15"),
                //xml
                new PGFieldType(name: "xml", type: "XML", version: "15"),
                //JSON
                new PGFieldType(name: "json", type: "JSON", version: "15"),
                new PGFieldType(name: "jsonb", type: "JSON", version: "15"),

//                //Range Types
//                new PGFieldType(name: "int4range", type: "Range Types", version: "15"),
//                new PGFieldType(name: "int8range", type: "Range Types", version: "15"),
//                new PGFieldType(name: "numrange", type: "Range Types", version: "15"),
//                new PGFieldType(name: "tsrange", type: "Range Types", version: "15"),
//                new PGFieldType(name: "tstzrange", type: "Range Types", version: "15"),
//                new PGFieldType(name: "daterange", type: "Range Types", version: "15"),
//                //Domain Types
//                new PGFieldType(name: "DOMAIN", type: "Domain Types", version: "15"),



        ]
    }


    static List<FieldType> getFieldTypes(String type = null, String version = "15") {
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
     * 转换TableInfo为PostgreSQL格式
     * @param tableInfo 输入的TableInfo，可能来自其他数据库
     */
    @Override
    void convert(TableInfo tableInfo) {
        if (!tableInfo?.fields) {
            return
        }

        tableInfo.fields.each { field ->
            String originalType = field.dataType?.toUpperCase()
            
            // 转换为PostgreSQL类型
            String pgType = convertToPgType(originalType)
            
            if (pgType) {
                field.dataType = pgType
            }
        }
    }

    /**
     * 将其他数据库类型转换为PostgreSQL类型
     */
    private String convertToPgType(String originalType) {
        if (!originalType) return originalType

        // MySQL类型映射
        switch (originalType) {
            case "TINYINT":
                return "smallint"
            case "SMALLINT":
                return "smallint"
            case "MEDIUMINT":
            case "INT":
            case "INTEGER":
                return "integer"
            case "BIGINT":
                return "bigint"
            case "FLOAT":
                return "real"
            case "DOUBLE":
                return "double precision"
            case "DECIMAL":
                return "decimal"
            case "CHAR":
                return "char"
            case "VARCHAR":
                return "varchar"
            case "BINARY":
                return "bytea"
            case "VARBINARY":
                return "bytea"
            case "BLOB":
                return "bytea"
            case "TEXT":
                return "text"
            case "DATE":
                return "date"
            case "DATETIME":
            case "TIMESTAMP":
                return "timestamp"
            case "TIME":
                return "time"
            case "YEAR":
                return "integer" // YEAR类型用integer代替
            case "JSON":
                return "jsonb" // MySQL的JSON对应PostgreSQL的jsonb
            case "ENUM":
            case "SET":
                return "varchar" // 枚举和集合类型用varchar代替
        }

        // Oracle类型映射
        switch (originalType) {
            case "NUMBER":
                return "numeric"
            case "FLOAT":
                return "real"
            case "BINARY_FLOAT":
                return "real"
            case "BINARY_DOUBLE":
                return "double precision"
            case "CHAR":
                return "char"
            case "VARCHAR2":
                return "varchar"
            case "NCHAR":
                return "char"
            case "NVARCHAR2":
                return "varchar"
            case "CLOB":
                return "text"
            case "NCLOB":
                return "text"
            case "BLOB":
                return "bytea"
            case "RAW":
                return "bytea"
            case "LONG RAW":
                return "bytea"
            case "DATE":
                return "date"
            case "TIMESTAMP":
                return "timestamp"
            case "TIMESTAMP WITH TIME ZONE":
                return "timestamptz"
            case "TIMESTAMP WITH LOCAL TIME ZONE":
                return "timestamptz"
            case "INTERVAL YEAR TO MONTH":
                return "interval"
            case "INTERVAL DAY TO SECOND":
                return "interval"
            case "XMLTYPE":
                return "xml"
            case "SDO_GEOMETRY":
                return "geometry"
        }

        // Doris类型映射
        switch (originalType) {
            case "BOOLEAN":
                return "boolean"
            case "TINYINT":
                return "smallint"
            case "SMALLINT":
                return "smallint"
            case "INT":
                return "integer"
            case "BIGINT":
                return "bigint"
            case "LARGEINT":
                return "numeric" // PostgreSQL没有LARGEINT，用numeric代替
            case "FLOAT":
                return "real"
            case "DOUBLE":
                return "double precision"
            case "DECIMAL":
                return "decimal"
            case "DATE":
                return "date"
            case "DATETIME":
                return "timestamp"
            case "CHAR":
                return "char"
            case "VARCHAR":
                return "varchar"
            case "ARRAY":
                return "jsonb" // 数组类型用jsonb代替
            case "MAP":
            case "STRUCT":
            case "JSON":
            case "VARIANT":
                return "jsonb" // 半结构化类型用jsonb代替
            case "HLL":
            case "BITMAP":
            case "QUANTILE_STATE":
            case "AGG_STATE":
                return "bytea" // 聚合类型用bytea存储
            case "IPv4":
            case "IPv6":
                return "inet" // IP类型用inet存储
        }

        // 如果没有找到映射，返回原类型
        return originalType
    }





}
