package com.yuezm.project.tugraph


import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.SessionConfig

/**
 * TuGraphDriver
 *
 * @author yzm
 * @version 1.0
 * @description TuGraphDriver
 * @date 2025/6/26 9:31
 */
class TuGraphDriver {
    protected final String bolt = "bolt://"


    String password = '73@TuGraph'
    String hostUrl = '127.0.0.1'
    String username = 'admin'
    String port = "7687"

    protected SessionConfig sessionConfig


    @Delegate
    Driver driver


    protected static TuGraphDriver doExecute(Closure closure) {
        assert closure != null: "closure is null"
        def driver = TuGraphConfig.instance.getUseDriver()
        if (!driver.driver) {
            driver.driver = GraphDatabase.driver(driver.bolt + driver.hostUrl + ":" + driver.port, AuthTokens.basic(driver.username, driver.password))
        }
        assert driver != null: "driver is null"
        closure.call(driver)
        return driver
    }

    protected static <V> V doExecute2(Closure<V> closure) {
        assert closure != null: "closure is null"
        def driver = TuGraphConfig.instance.getUseDriver()
        if (!driver.driver) {
            driver.driver = GraphDatabase.driver(driver.bolt + driver.hostUrl + ":" + driver.port, AuthTokens.basic(driver.username, driver.password))
        }
        assert driver != null: "driver is null"
        return closure.call(driver)
    }

    /**
     * 创建图
     * @param dbName
     * @return
     */
    TuGraphDriver createGraph(String dbName) {
        assert dbName != null && dbName.trim().length() > 0: "dbName is null"
        return doExecute { TuGraphDriver d ->
            d.session().run("CALL dbms.graph.createGraph('${dbName}')")
        }
    }

    /**
     * 判断图是否存在
     * @param dbName
     * @return
     *
     * */

    Boolean existsGraph(String dbName) {
        boolean exists = false
        assert dbName != null && dbName.trim().length() > 0: "dbName is null"
        try {
            doExecute2 { TuGraphDriver d ->
                def res = d.session(SessionConfig.forDatabase(dbName)).run("RETURN 1")
                exists = true
            }
        } catch (Exception e) {

        }
        return exists
    }


    /**
     * 删除图
     * @param dbName
     * @return
     */
    TuGraphDriver deleteGraph(String dbName) {
        assert dbName != null && dbName.trim().length() > 0: "dbName is null"
        return doExecute { TuGraphDriver d ->
            d.session().run("CALL dbms.graph.deleteGraph('${dbName}')")
        }
    }


    /**
     * 选中图
     * @param dbName
     * @return
     */
    TuGraphDriver checkedGraph(String dbName) {
        assert dbName != null && dbName.trim().length() > 0: "dbName is null"
        return doExecute { TuGraphDriver d ->
            d.sessionConfig = SessionConfig.forDatabase(dbName)
        }
    }

    /**
     * 删除所有的点边数据和图schema
     * @param dbName
     * @return
     */
    TuGraphDriver dropDB(String dbName = null) {
        return doExecute { TuGraphDriver d ->
            if (dbName != null) {
                d.session(SessionConfig.forDatabase(dbName)).run("CALL db.dropDB()")
                return
            }
            d.session(d.sessionConfig).run("CALL db.dropDB()")
        }
    }

    /**
     * 只删除所有点边数据, 保留图schema
     * @param dbName
     * @return
     */
    TuGraphDriver dropAllVertex(String dbName = null) {
        return doExecute { TuGraphDriver d ->
            if (dbName != null) {
                d.session(SessionConfig.forDatabase(dbName)).run("CALL db.dropAllVertex()")
                return
            }
            d.session(d.sessionConfig).run("CALL db.dropAllVertex()")
        }
    }

    /**
     * 查看图schema
     * @return
     */
    def getGraphSchema(String dbName = null) {
        return doExecute2 { TuGraphDriver d ->
            if(dbName != null){
                return d.session(SessionConfig.forDatabase(dbName)).run("CALL dbms.graph.getGraphSchema()")
            }
            return d.session(sessionConfig).run("CALL dbms.graph.getGraphSchema()")
        }
    }

    /**
     * 列出所有子图
     * @return
     */
    def listGraphs() {
        return doExecute { TuGraphDriver d ->
            d.session().run("CALL dbms.graph.listGraphs()")
        }
    }

    /**
     * 刷新子图文件系统缓存数据
     * @return
     */
    def flushDB(String dbName = null) {
        return doExecute { TuGraphDriver d ->
            assert !(dbName == null && d.sessionConfig == null): "dbName or sessionConfig is null"
            if (dbName != null) {
                d.session(SessionConfig.forDatabase(dbName)).run("CALL dbms.graph.flushDB()")
                return
            }
            d.session(d.sessionConfig).run("CALL dbms.graph.flushDB()")
        }
    }

    /**
     * 创建点类型
     * @param dbName
     * @param json
     * @return
     */
    TuGraphDriver createVertexLabelByJson(String dbName = null, String json) {
        assert dbName != null && dbName.trim().length() > 0: "dbName is null"
        assert json != null && json.trim().length() > 0: "json is null"
        return doExecute { TuGraphDriver d ->
            if (dbName) {
                d.session(SessionConfig.forDatabase(dbName)).run("CALL db.createVertexLabelByJson(\$json)",[json: json])
                return
            }
            d.session(d.sessionConfig).run("CALL db.createVertexLabelByJson(\$json)", [json: json])
        }
    }

    /**
     * 查看点类型
     * @param dbName
     * @param nodeName
     * @return
     */
    def getVertexSchema(String dbName = null, String nodeName) {
        assert nodeName != null && nodeName.trim().length() > 0: "nodeName is null"
        return doExecute2 { TuGraphDriver d ->
            if (dbName) {
                d.session(SessionConfig.forDatabase(dbName)).run("CALL db.getVertexSchema('$nodeName')")
                return
            }
            d.session(d.sessionConfig).run("CALL db.getVertexSchema('$nodeName')")
        }
    }


    /**
     * 删除点类型 (该操作会同步删除所有该类型的点数据，数据量大的时候，有时间消耗。)
     * @param dbName
     * @param nodeName
     * @return
     */
    TuGraphDriver deleteLabel(String dbName = null, String nodeName) {
        assert nodeName != null && nodeName.trim().length() > 0: "nodeName is null"
        return doExecute { TuGraphDriver d ->
            if (dbName) {
                d.session(SessionConfig.forDatabase(dbName)).run("CALL db.deleteLabel('vertex', '$nodeName')")
                return
            }
            d.session(d.sessionConfig).run("CALL db.deleteLabel('vertex', '$nodeName')")
        }
    }


    /**
     * 创建边类型
     * @param dbName
     * @param json
     * @return
     */
    TuGraphDriver createEdgeLabelByJson(String dbName = null, String json) {
        assert dbName != null && dbName.trim().length() > 0: "dbName is null"
        assert json != null && json.trim().length() > 0: "json is null"
        return doExecute { TuGraphDriver d ->
            if (dbName) {
                d.session(SessionConfig.forDatabase(dbName)).run("CALL db.createEdgeLabelByJson(\$json)", [json: json])
                return
            }
            d.session(d.sessionConfig).run("CALL db.createEdgeLabelByJson(\$json)", [json: json])
        }
    }

    /**
     * 查看边类型
     * @param dbName
     * @param edgeName
     * @return
     */
    def getEdgeSchema(String dbName = null, String edgeName) {
        assert edgeName != null && edgeName.trim().length() > 0: "edgeName is null"
        return doExecute2 { TuGraphDriver d ->
            if (dbName) {
                d.session(SessionConfig.forDatabase(dbName)).run("CALL db.getEdgeSchema('$edgeName')")
                return
            }
            d.session(d.sessionConfig).run("CALL db.getEdgeSchema('$edgeName')")
        }
    }

    /**
     * 删除边类型 (该操作会同步删除所有该类型的边数据，数据量大的时候，有时间消耗。)
     * @param dbName
     * @param edgeName
     * @return
     */
    TuGraphDriver deleteEdgeLabel(String dbName = null, String edgeName) {
        assert edgeName != null && edgeName.trim().length() > 0: "edgeName is null"
        return doExecute { TuGraphDriver d ->
            if (dbName) {
                d.session(SessionConfig.forDatabase(dbName)).run("CALL db.deleteLabel('edge', '$edgeName')")
                return
            }
            d.session(d.sessionConfig).run("CALL db.deleteLabel('edge', '$edgeName')")
        }
    }

    /**
     * 判断点是否已经存在，存在返回true，不存在返回false
     * @param dbName
     * @param name
     * @return
     */
    Boolean isExistPoint(String dbName = null, String name) {
        assert name != null && name.trim().length() > 0: "name is null"
        return doExecute2 { TuGraphDriver d ->
            def f = false
            if (dbName) {
                try {
                    def res = d.session(SessionConfig.forDatabase(dbName)).run("MATCH (n:$name) RETURN count(n) > 0 AS isEisxt")
                    def isExist = res.single().get("isExist")
                    f = true
                } catch (Exception e) {

                }
                return f
            }
            try {
                d.session(d.sessionConfig).run("MATCH (n:$name) RETURN count(n) > 0 AS isEisxt")
                def isExist = res.single().get("isExist")
                f = true
            } catch (Exception e) {

            }
            return f
        }
    }

    /**
     * 获取点所有数据
     * @param dbName
     * @param nodeName
     * @return
     */
    def getVertexAllData(String dbName = null, String nodeName) {
        assert nodeName != null && nodeName.trim().length() > 0: "labelName is null"
        return doExecute2 { TuGraphDriver d ->
            def res
            if (dbName) {
                res = d.session(SessionConfig.forDatabase(dbName)).run("MATCH (n:$nodeName) RETURN n")
                return res
            }
            res = d.session(d.sessionConfig).run("MATCH (n:$nodeName) RETURN n")
            return res
        }
    }

    /**
     * 判断边是否已经存在，存在返回true，不存在返回false
     * @param dbName
     * @param name
     * @return
     */
    Boolean isExistEdge(String dbName = null, String name) {
        assert name != null && name.trim().length() > 0: "name is null"
        return doExecute2 { TuGraphDriver d ->
            def f = false
            if (dbName) {
                try {
                    def res = d.session(SessionConfig.forDatabase(dbName)).run("MATCH ()-[r:$name]->() RETURN r LIMIT 1")
                    res.single()
                    f = true
                } catch (Exception e) {

                }
                return f
            }
            try {
                def res =d.session(d.sessionConfig).run("MATCH ()-[r:$name]->() RETURN r LIMIT 1")
                res.single()
                f = true
            } catch (Exception e) {

            }
            return f
        }
    }

    /**
     * 获取边所有数据
     * @param dbName
     * @param edgeName
     * @return
     */
    def getEdgeAllData(String dbName = null, String edgeName) {
        assert edgeName != null && edgeName.trim().length() > 0: "edgeName is null"
        return doExecute2 { TuGraphDriver d ->
            def res
            if (dbName) {
                res = d.session(SessionConfig.forDatabase(dbName)).run("MATCH ()-[r:$edgeName]->() RETURN r")
                return res
            }
            res = d.session(d.sessionConfig).run("MATCH ()-[r:$edgeName]->() RETURN r")
            return res
        }
    }
    /**
     * 批量upsert点数据
     * @param dbName
     * @param nodeName
     * @param datas
     * @return
     */
    <T extends Serializable> TuGraphDriver upsertNodeData(String dbName = null, String nodeName, List<T> datas) {
        assert nodeName != null && nodeName.trim().length() > 0: "nodeName is null"
        assert datas != null && datas.size() > 0: "datas is null"
        return doExecute { TuGraphDriver d ->
            if (dbName) {
                d.session(SessionConfig.forDatabase(dbName)).run("CALL db.upsertVertex('$nodeName', \$data)", [data: datas])
                return
            }
            d.session(d.sessionConfig).run("CALL db.upsertVertex('$nodeName', \$data)", [data: datas])
        }
    }

    /**
     * 清空节点
     * @param dbName
     * @param nodeName
     * @return
     */
    TuGraphDriver cleanNodeData(String dbName = null, String nodeName){
        assert nodeName != null && nodeName.trim().length() > 0: "nodeName is null"
        return doExecute { TuGraphDriver d ->
            if (dbName) {
                d.session(SessionConfig.forDatabase(dbName)).run("MATCH (n:$nodeName) DETACH DELETE n")
                return
            }
            d.session(d.sessionConfig).run("MATCH (n:$nodeName) DETACH DELETE n")
        }
    }



    /**
     * 批量upsert边数据
     * @param dbName
     * @param EdgeName
     * @param datas
     * @return
     */
    <T extends Serializable> TuGraphDriver upsertEdgeData(String dbName = null, String edgeName, List<T> nodes, List<T> datas) {
        assert edgeName != null && edgeName.trim().length() > 0: "edgeName is null"
        assert datas != null && datas.size() > 0: "datas is null"
        assert nodes != null && nodes.size() == 2: "nodes is null or size != 2"
        return doExecute { TuGraphDriver d ->
            if(dbName){
                d.session(SessionConfig.forDatabase(dbName)).run("CALL db.upsertEdge('$edgeName', \$node0, \$node1, \$data)", [
                        node0: nodes[0],
                        node1: nodes[1],
                        data: datas
                ])
                return
            }
            d.session(d.sessionConfig).run("CALL db.upsertEdge('$edgeName', \$node0, \$node1, \$data)", [
                    node0: nodes[0],
                    node1: nodes[1],
                    data: datas
            ])
        }
    }


    /**
     * 批量upsert边数据
     * @param dbName
     * @param EdgeName
     * @param datas
     * @param props
     * @return
     */
    <T extends Serializable> TuGraphDriver upsertEdgeData2(String dbName = null, String edgeName, List<T> nodes, List<String> props, List<T> datas){
        assert edgeName != null && edgeName.trim().length() > 0: "edgeName is null"
        assert datas != null && datas.size() > 0: "datas is null"
        assert nodes != null && nodes.size() == 2: "nodes is null or size != 2"

        return doExecute { TuGraphDriver d ->
            if(dbName){
                    datas.each {
                        def propAssignments = props.collect { p -> " ${p}: '${it[p]}'" }.join(", ")
                        d.session(SessionConfig.forDatabase(dbName)).run(
                                " MATCH (s:${nodes[0]?.type} {${nodes[0]?.key}: '$it.from_val' }), (t:${nodes[1]?.type} {${nodes[1]?.key}: '$it.to_val'}) "+
                                        " CREATE (s)-[:${edgeName} {${propAssignments}}]->(t)"
                        )
                    }
                return
            }
//            d.session(d.sessionConfig).run("UNWIND \$edges AS edge " +
//                    "MATCH (s:${nodes[0]?.type} {${nodes[0]?.key}: edge.from_val }), (t:${nodes[1]?.type} {${nodes[1]?.key}: edge.to_val}) "+
//                    "CREATE (s)-[:${edgeName} {${propAssignments}}]->(t)", [edges: datas])
            datas.each {
                def propAssignments = props.collect { p -> " ${p}: '${it[p]}'" }.join(", ")
                d.session(SessionConfig.forDatabase(dbName)).run(
                        " MATCH (s:${nodes[0]?.type} {${nodes[0]?.key}: '$it.from_val' }), (t:${nodes[1]?.type} {${nodes[1]?.key}: '$it.to_val'}) "+
                                " CREATE (s)-[:${edgeName} {${propAssignments}}]->(t)"
                )
            }
        }
    }



    /**
     * 清除边
     * @param dbName
     * @param edgeName
     * @return
     */
    TuGraphDriver cleanEdgeData(String dbName = null, String edgeName){
        assert edgeName != null && edgeName.trim().length() > 0: "edgeName is null"
        return doExecute { TuGraphDriver d ->
            if (dbName) {
                d.session(SessionConfig.forDatabase(dbName)).run("MATCH ()-[r:$edgeName]->() DELETE r")
                return
            }
            d.session(d.sessionConfig).run("MATCH ()-[r:$edgeName]->() DELETE r")
        }
    }


    /**
     * 根据当前节点，正向查询后面的所有节点
     * @param dbName
     * @param nodeName
     */
    def getPositiveNodes(String dbName = null, String nodeName) {
        assert nodeName != null && nodeName.trim().length() > 0: "nodeName is null"
        return doExecute2 { TuGraphDriver d ->
            if (dbName) {
                def res = d.session(SessionConfig.forDatabase(dbName)).run("MATCH (startNode:$nodeName)-[*..999]->(n1)\n" +
                        "WHERE NOT startNode = n1\n" +
                        "RETURN DISTINCT labels(n1) AS label")
                return res
            }
            return d.session(d.sessionConfig).run("MATCH (startNode:$nodeName)-[*..999]->(n1)\n" +
                    "WHERE NOT startNode = n1\n" +
                    "RETURN DISTINCT labels(n1) AS label")
        }
    }


    /**
     * 根据当前节点，反向查询前面的所有数据
     * @param dbName
     * @param nodeName
     * @return
     */
    def getNegativeNodes(String dbName = null, String nodeName) {
        assert nodeName != null && nodeName.trim().length() > 0: "nodeName is null"
        return doExecute2 { TuGraphDriver d ->
            if (dbName) {
                def res = d.session(SessionConfig.forDatabase(dbName)).run("MATCH (n1)-[*..999]->(endNode:$nodeName)\n" +
                        "WHERE NOT n1 = endNode\n" +
                        "RETURN DISTINCT labels(n1) AS label")
                return res
            }
            return d.session(d.sessionConfig).run("MATCH (n1)-[*..999]->(endNode:$nodeName)\n" +
                    "WHERE NOT n1 = endNode\n" +
                    "RETURN DISTINCT labels(n1) AS label")
        }
    }
}
