package com.yuezm.project.sql.pg

import com.yuezm.project.sql.DatasourceProperties
import com.yuezm.project.sql.PoolConfig
import com.yuezm.project.sql.SqlPoolHandler
import com.yuezm.project.sql.TableField
import com.yuezm.project.sql.TableInfo
import com.yuezm.project.sql.Wrapper



/**
 * PGSqlPoolHandler
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/12/24 11:20
 */
class PGSqlPoolHandler extends SqlPoolHandler {

    PGSqlPoolHandler(DatasourceProperties datasourceProperties, Map<String, Object> otherPoolConfig = [:]) {
        super(datasourceProperties, otherPoolConfig)
    }

    @Override
    boolean isSupportGis() {
        String query = "SELECT COUNT(*) count FROM pg_extension WHERE extname = 'postgis'"
        long count = super.firstRow(query).count as long
        return count > 0
    }

    @Override
    String mapJavaToSQL(String javaType) {
        String sqlType
        switch (javaType) {
            case "java.lang.Integer":
            case "Integer":
                sqlType = "int4"
                break
            case "java.lang.Long":
            case "Long":
                sqlType = "int8"
                break
            case "java.lang.Short":
            case "Short":
                sqlType = "int2"
                break
            case "java.lang.Float":
            case "Float":
                sqlType = "float4"
                break
            case "java.lang.Double":
            case "Double":
                sqlType = "float8"
                break
            case "BigDecimal":
                sqlType = "numeric"
                break
            case "java.lang.Boolean":
            case "Boolean":
                sqlType = "boolean"
                break
            case "java.lang.String":
            case "String":
                sqlType = "varchar(1024)"
                break
            case "java.lang.Character":
            case "Character":
                sqlType = "char"
                break
            case "java.time.LocalDate":
            case "LocalDate":
                sqlType = "date"
                break
            case "java.time.LocalTime":
            case "LocalTime":
                sqlType = "time"
                break
            case "java.time.LocalDateTime":
            case "java.util.Date":
            case "Date":
            case "LocalDateTime":
                sqlType = "timestamp"
                break
            case "byte[]":
            case "java.sql.Blob":
                sqlType = "bytea"
                break
            case "java.util.UUID":
            case "UUID":
                sqlType = "uuid"
                break
            case "java.util.List":
            case "java.util.ArrayList":
            case "java.util.Set":
            case "java.util.HashSet":
            case "java.util.TreeSet":
            case "java.util.LinkedHashSet":
            case "List":
            case "ArrayList":
            case "Set":
            case "HashSet":
            case "TreeSet":
            case "LinkedHashSet":
                sqlType = "jsonb"
                break
            case "java.util.Map":
            case "java.util.HashMap":
            case "java.util.TreeMap":
            case "java.util.LinkedHashMap":
            case "Map":
            case "HashMap":
            case "TreeMap":
            case "LinkedHashMap":
                sqlType = "jsonb"
                break
            case "Geometry":
            case "geometry":
                sqlType = "text"
                break
            default:
                throw new IllegalArgumentException("Unsupported Java type: ${javaType}")
        }
        return sqlType
    }

    @Override
    boolean addTableMemo(String tableName, String memo) {
        String sql = "COMMENT ON TABLE $tableName IS '$memo';"
        try {
            execute(sql)
        } catch (Exception e) {
            e.printStackTrace()
            return false
        }
        return true
    }

    @Override
    boolean dropTable(String tableName) {
        String sql = "DROP TABLE IF EXISTS $tableName;"
        try {
            execute(sql)
        } catch (Exception e) {
            e.printStackTrace()
        }
        return true
    }

    @Override
    Wrapper getWrapper() {
        if (selfWrapper == null) {
            selfWrapper = new PGSqlWrapper()
        }
        return selfWrapper
    }

    @Override
    Number getTableDataCapacity(String tableName, String schema = null) {
        String sql = "SELECT pg_total_relation_size('$tableName') AS total_size"
        return firstRow(sql)?["total_size"] as Number
    }

    @Override
    List<Map<String, Object>> getTablePrimarys(String tableName, String schema = null) {
        String sql = "SELECT kcu.column_name \"COLUMN_NAME\" " +
                "FROM information_schema.table_constraints tc\n" +
                "JOIN information_schema.key_column_usage kcu\n" +
                "  ON tc.constraint_name = kcu.constraint_name\n" +
                "  AND tc.table_schema = kcu.table_schema\n" +
                "WHERE tc.constraint_type = 'PRIMARY KEY'\n" +
                "  AND tc.table_name = '$tableName'\n" +
                "  AND tc.table_schema = '$schema';"
        return rows(sql)
    }

    //TODO 未实现

    @Override
    TableInfo getTableInfo(String tableName, String schema) {
        def info = new TableInfo()
        String sql = "SELECT\n" +
                "    obj_description(relfilenode, 'pg_class') AS table_comment\n" +
                "FROM pg_class\n" +
                "WHERE relname = '$tableName'\n" +
                "  AND relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = '$schema');"
        def row = firstRow(sql)
        if (row) {
            info.comment = row?["table_comment"]
        }
        sql = "SELECT\n" +
                "    c.ordinal_position AS column_id,\n" +
                "    c.column_name,\n" +
                "    c.data_type,\n" +
                "    c.character_maximum_length,\n" +
                "    c.numeric_precision,\n" +
                "    c.numeric_scale,\n" +
                "    c.is_nullable,\n" +
                "    c.column_default,\n" +
                "    d.description AS column_comment,\n" +
                "    CASE WHEN kcu.column_name IS NOT NULL THEN 'YES' ELSE 'NO' END AS is_primary_key\n" +
                "FROM information_schema.columns c\n" +
                "LEFT JOIN pg_catalog.pg_description d\n" +
                "       ON d.objoid = (SELECT oid FROM pg_class WHERE relname = c.table_name\n" +
                "                      AND relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = c.table_schema))\n" +
                "      AND d.objsubid = c.ordinal_position\n" +
                "LEFT JOIN information_schema.key_column_usage kcu\n" +
                "       ON c.table_name = kcu.table_name\n" +
                "      AND c.table_schema = kcu.table_schema\n" +
                "      AND c.column_name = kcu.column_name\n" +
                "      AND kcu.constraint_name IN (\n" +
                "            SELECT constraint_name\n" +
                "            FROM information_schema.table_constraints\n" +
                "            WHERE table_name = c.table_name\n" +
                "              AND table_schema = c.table_schema\n" +
                "              AND constraint_type = 'PRIMARY KEY'\n" +
                "       )\n" +
                "WHERE c.table_name = '$tableName'\n" +
                "  AND c.table_schema = '$schema'\n" +
                "ORDER BY c.ordinal_position;"
        def results = rows(sql)
        info.fields = results.collect {
            return new TableField(
                    colName: it["column_name"],
                    dataType: it["data_type"],
                    comment: it["column_comment"],
                    isNullable: it["is_nullable"] == "YES",
                    defaultValue: it["column_default"],
                    scale: it["numeric_scale"] as Integer,
                    length: it["character_maximum_length"] as Integer,
                    isPrimaryKey: it["is_primary_key"] == "YES"
            )
        }
        return info
    }


    static void main(String[] args) {

        /**
         * 数据源连接池复用服务 框架
         * Aeron 是一个为低延迟、高吞吐、可预测性能而设计的消息传输框架，用来在进程之间或机器之间高效传输字节流。
         *   1.RPC / 请求响应
         *  2. SQL 执行代理
         *  3. 游戏服务器
         *  4. 实时风控
         *  5. 撮合引擎
         *  6. 低延迟微服务通信
         *  7.本地多进程通信（IPC）
         * Protocol 统一通信语义（语法与语义约定）
         * 1. 序列化体积小
             二进制编码，相比 JSON / XML 通常 小 3～10 倍
             使用 Varint 对整数进行压缩
             不携带字段名，仅使用 field number
           2.生成代码，无反射（或极少反射）生成代码，无反射（或极少反射）
             结构化内存布局，CPU Cache 友好
             在高并发、低延迟场景（RPC、消息系统）中：
             明显优于 JSON、XML
             通常略慢于极致手写二进制协议，但可维护性远高
           3.明确的 Schema（IDL）
            通过 .proto 文件： 明确字段类型
            明确字段编号（tag）
            明确必填 / 可选 / repeated
            这带来：
            编译期校验
            消除“弱约束 JSON”的隐性 Bug
         */
        def properties = new DatasourceProperties(
                url: "jdbc:postgresql://192.168.110.9:25432/demo?stringtype=unspecified&currentSchema=cdc",
                username: "postgres",
                password: "skzz@2023",
                driverClassName: "org.postgresql.Driver")

        /**
         * 1. 配置服务端连接信息
         * clientHost 当前服务接收地址
         * clientPort 当前服务接收端口
         * clientSteamId 当前服务接收流id
         * serverHost 服务端地址
         */
//        PoolConfig.instance.clientHost = "192.168.110.222"
//        PoolConfig.instance.clientPort = 38881
//        PoolConfig.instance.clientSteamId = 2500
//        PoolConfig.instance.serverHost = "192.168.110.222"
        /**
         * 2. 注册数据源
         *  创建实例会自动向服务端注册数据源，如果服务端这个数据被注册了，
         *  服务端不会重新注册数据源，会直接返回key,key会保存在handler中
         */
        def handler = new PGSqlPoolHandler(properties)
        /**
         * 3.使用handler执行sql
         */
//        def res = handler.firstRow("SELECT * FROM cccc")
//        def res = handler.query("SELECT * FROM cccc", [params:[:]], """
//def result = [:]
//        def list = []
//        def dispose = { java.sql.ResultSet rs ->
//            java.sql.ResultSetMetaData metaData = rs.getMetaData()
//            int columnCount = metaData.getColumnCount()
//            result.fields = (1.. columnCount).collect {
//                def f
//                try {
//                    String columnName= metaData.getColumnLabel(it)
//                    if(!columnName){
//                        columnName = metaData.getColumnName(it)
//                    }
//                    f = [field: columnName, dataType: metaData.getColumnTypeName(it)]
//                }catch (java.sql.SQLException e){
//                    e.printStackTrace()
//                }
//                f
//            }
//            while (rs.next()){
//                def data = [:]
//                (1.. columnCount).each{
//                    String columnName= metaData.getColumnLabel(it)
//                    if(!columnName){
//                        columnName = metaData.getColumnName(it)
//                    }
//                    data[columnName] = rs.getObject(it)
//                }
//                list << data
//            }
//        }
//
//        if(params.size() > 0){
//            sqlHandler.query(sqlStr, params, dispose)
//        }else {
//            sqlHandler.query(sqlStr, dispose)
//        }
//        result.datas=list
//        return result
//""")
//        println "更新之前：res.name:${res?.name}"
//        handler.execute([id: '1', name: "6sdfsfd"], "update test1 set name = :name where id = :id")
//        res = handler.firstRow([id:'1'], "select * from test1 where id = :id ")
//        println "更新之后：res.name:${res?.name}"
//        /**
//         * 4.使用查询功能,自定义返回结果（里面能用statement 和rowset,高度自定义）
//         * 4.1 内部参数-固定参数
//         *     (1).sqlHandler,sql执行器，具体使用参照groovy.sql.Sql
//         *     (2).JSON 可以序列化对象，已经特殊处理，不会把中文转成unicode,不可反序列化对象, 具体使用参考groovy.json.JsonGenerator
//         *     (3).sqlStr, 传入的sql
//         * 4.2 内部参数-可变参数
//         *      (1)args. 用户传入单个参数，使用 args[0]能获取到参数
//         *      (2)args. 用户传入集合参数，使用 args 得到这个集合参数
//         *      (3)任意参数，用户传入obj,如下案例，直接使用里面key(properties)
//         */
//        def res2 = handler.rows([id:'1'],"select * from test1 where id = :id", """
//            sqlHandler.rows(['id': id], sqlStr)
//""")
        def s = System.currentTimeMillis()
        println "search start time: $s"
        def res = handler.rows("SELECT * FROM cccc")
        def e = System.currentTimeMillis()
        println "search end time: $e"
        println "cost: ${(e - s)/1000} s"

    }


}
