package com.yuezm.project.sql.oracle

import com.yuezm.project.sql.FieldType
import com.yuezm.project.sql.TableInfo

/**
 * OracleFieldType
 *
 * Oracle Database 19c supported field types
 *
 * @author yzm
 * @version 1.1
 * @date 2026/2/2
 */
class OracleFieldType extends FieldType {

    final static List<OracleFieldType> types = []

    static {
        /**
         * Oracle Database 19c
         * Reference:
         * https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Data-Types.html
         */

        // =========================
        // Numeric Datatypes
        // =========================
        types.addAll([
                new OracleFieldType(name: "NUMBER", type: "Numeric", version: "19c"),
                new OracleFieldType(name: "FLOAT", type: "Numeric", version: "19c"),
                new OracleFieldType(name: "BINARY_FLOAT", type: "Numeric", version: "19c"),
                new OracleFieldType(name: "BINARY_DOUBLE", type: "Numeric", version: "19c")
        ])

        // =========================
        // Character Datatypes
        // =========================
        types.addAll([
                new OracleFieldType(name: "CHAR", type: "Character", version: "19c"),
                new OracleFieldType(name: "VARCHAR2", type: "Character", version: "19c"),
                new OracleFieldType(name: "NCHAR", type: "Character", version: "19c"),
                new OracleFieldType(name: "NVARCHAR2", type: "Character", version: "19c")
        ])

        // =========================
        // Datetime Datatypes
        // =========================
        types.addAll([
                new OracleFieldType(name: "DATE", type: "Datetime", version: "19c"),
                new OracleFieldType(name: "TIMESTAMP", type: "Datetime", version: "19c"),
                new OracleFieldType(name: "TIMESTAMP WITH TIME ZONE", type: "Datetime", version: "19c"),
                new OracleFieldType(name: "TIMESTAMP WITH LOCAL TIME ZONE", type: "Datetime", version: "19c"),
                new OracleFieldType(name: "INTERVAL YEAR TO MONTH", type: "Datetime", version: "19c"),
                new OracleFieldType(name: "INTERVAL DAY TO SECOND", type: "Datetime", version: "19c")
        ])

        // =========================
        // LOB Datatypes
        // =========================
        types.addAll([
                new OracleFieldType(name: "CLOB", type: "LOB", version: "19c"),
                new OracleFieldType(name: "NCLOB", type: "LOB", version: "19c"),
                new OracleFieldType(name: "BLOB", type: "LOB", version: "19c"),
                new OracleFieldType(name: "BFILE", type: "LOB", version: "19c")
        ])

        // =========================
        // Binary Datatypes
        // =========================
        types.addAll([
                new OracleFieldType(name: "RAW", type: "Binary", version: "19c"),
                new OracleFieldType(name: "LONG RAW", type: "Binary", version: "19c")
        ])

        // =========================
        // Row Identifier
        // =========================
        types.addAll([
                new OracleFieldType(name: "ROWID", type: "Other", version: "19c"),
                new OracleFieldType(name: "UROWID", type: "Other", version: "19c")
        ])

        // =========================
        // XML / Spatial
        // =========================
        types.addAll([
                new OracleFieldType(name: "XMLTYPE", type: "XML", version: "19c"),
                new OracleFieldType(name: "SDO_GEOMETRY", type: "Spatial", version: "19c")
        ])
    }

    /**
     * Get field types by category and version
     *
     * @param type     Numeric / Character / Datetime / LOB / Binary / Spatial / XML / Other
     * @param version  Oracle version, default 19c
     * @return filtered field type list
     */
    static List<FieldType> getFieldTypes(String type = null, String version = "19c") {
        List<FieldType> fieldTypeList = types

        if (type) {
            fieldTypeList = fieldTypeList.findAll {
                it.type == type
            }
        }

        if (version) {
            fieldTypeList = fieldTypeList.findAll {
                it.version == version
            }
        }

        return fieldTypeList
    }

    /**
     * 转换TableInfo为Oracle格式
     * @param tableInfo 输入的TableInfo，可能来自其他数据库
     */
    @Override
    void convert(TableInfo tableInfo) {
        if (!tableInfo?.fields) {
            return
        }

        tableInfo.fields.each { field ->
            String originalType = field.dataType?.toUpperCase()
            
            // 转换为Oracle类型
            String oracleType = convertToOracleType(originalType)
            
            if (oracleType) {
                field.dataType = oracleType
            }
        }
    }

    /**
     * 将其他数据库类型转换为Oracle类型
     */
    private String convertToOracleType(String originalType) {
        if (!originalType) return originalType

        // MySQL类型映射
        switch (originalType) {
            case "TINYINT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "INT":
            case "INTEGER":
                return "NUMBER"
            case "BIGINT":
                return "NUMBER"
            case "FLOAT":
                return "BINARY_FLOAT"
            case "DOUBLE":
                return "BINARY_DOUBLE"
            case "DECIMAL":
                return "NUMBER"
            case "CHAR":
                return "CHAR"
            case "VARCHAR":
                return "VARCHAR2"
            case "BINARY":
                return "RAW"
            case "VARBINARY":
                return "RAW"
            case "BLOB":
                return "BLOB"
            case "TEXT":
                return "CLOB"
            case "DATE":
                return "DATE"
            case "DATETIME":
            case "TIMESTAMP":
                return "TIMESTAMP"
            case "TIME":
                return "DATE" // Oracle没有专门的TIME类型，用DATE代替
            case "YEAR":
                return "NUMBER"
            case "JSON":
                return "XMLTYPE" // Oracle没有JSON类型，用XMLTYPE代替
            case "ENUM":
            case "SET":
                return "VARCHAR2" // 枚举和集合类型用VARCHAR2代替
        }

        // PostgreSQL类型映射
        switch (originalType) {
            case "SMALLINT":
            case "INTEGER":
            case "BIGINT":
                return "NUMBER"
            case "DECIMAL":
            case "NUMERIC":
                return "NUMBER"
            case "REAL":
                return "BINARY_FLOAT"
            case "DOUBLE PRECISION":
                return "BINARY_DOUBLE"
            case "SMALLSERIAL":
            case "SERIAL":
            case "BIGSERIAL":
                return "NUMBER"
            case "CHARACTER VARYING":
            case "VARCHAR":
                return "VARCHAR2"
            case "CHARACTER":
            case "CHAR":
                return "CHAR"
            case "TEXT":
                return "CLOB"
            case "BYTEA":
                return "BLOB"
            case "BOOLEAN":
                return "NUMBER(1)" // Oracle用NUMBER(1)表示布尔值
            case "DATE":
                return "DATE"
            case "TIME":
            case "TIMETZ":
                return "DATE" // Oracle没有专门的TIME类型
            case "TIMESTAMP":
            case "TIMESTAMPTZ":
                return "TIMESTAMP"
            case "INTERVAL":
                return "INTERVAL DAY TO SECOND"
            case "JSON":
            case "JSONB":
                return "XMLTYPE"
            case "UUID":
                return "RAW(16)" // UUID用RAW(16)存储
            case "XML":
                return "XMLTYPE"
            case "POINT":
            case "GEOMETRY":
                return "SDO_GEOMETRY"
        }

        // Doris类型映射
        switch (originalType) {
            case "BOOLEAN":
                return "NUMBER(1)"
            case "TINYINT":
            case "SMALLINT":
            case "INT":
            case "BIGINT":
            case "LARGEINT":
                return "NUMBER"
            case "FLOAT":
                return "BINARY_FLOAT"
            case "DOUBLE":
                return "BINARY_DOUBLE"
            case "DECIMAL":
                return "NUMBER"
            case "DATE":
                return "DATE"
            case "DATETIME":
                return "TIMESTAMP"
            case "CHAR":
                return "CHAR"
            case "VARCHAR":
                return "VARCHAR2"
            case "ARRAY":
            case "MAP":
            case "STRUCT":
            case "JSON":
            case "VARIANT":
                return "XMLTYPE" // 半结构化类型用XMLTYPE代替
            case "HLL":
            case "BITMAP":
            case "QUANTILE_STATE":
            case "AGG_STATE":
                return "RAW" // 聚合类型用RAW存储
            case "IPv4":
            case "IPv6":
                return "VARCHAR2" // IP类型用VARCHAR2存储
        }

        // 如果没有找到映射，返回原类型
        return originalType
    }
}
