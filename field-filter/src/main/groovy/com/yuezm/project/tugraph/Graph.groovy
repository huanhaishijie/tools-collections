package com.yuezm.project.tugraph


import cn.hutool.json.JSONUtil
import com.yuezm.project.common.SnowFlakeWorker
import groovy.json.JsonSlurper
import org.neo4j.driver.Result
import org.neo4j.driver.internal.InternalRelationship

/**
 * Graph
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/6/27 11:38
 */
class Graph implements Serializable{
    String name
    List<? extends Node> nodeList

    @Delegate
    TuGraphDriver driver = TuGraphDriver.doExecute{}

    /**
     * 持久化
     * @return
     */
    Boolean persistence(){

        Map<String, List<Map<String, Object>>> nodeData = [:]
        Map<String, Map<String, List<Map<String, Object>>>> edgeData = [:]
        Map<String, String> nodePrimary = [:]
        try {
            TuGraphDriver.doExecute { TuGraphDriver d ->
                if(!d.existsGraph(name)){
                    d.createGraph(name)
                }
            }
            if(!nodeList || nodeList.size() == 0){
                return true
            }
            List<Edge> edgeList = []

            TuGraphDriver.doExecute { TuGraphDriver d ->
                nodeList.each { Node n ->
                    if(!n.node_name){
                        n.node_name = n.getClass().getName()
                    }

                    def f = d.isExistPoint(name, n.node_name)
                    def clazz = n.getClass()
                    java.lang.reflect.Field[] fields = clazz.getFields() + clazz.getDeclaredFields()
                    fields = fields.findAll {
                        return !(it.getName().contains("\$") || it.getName().equals("metaClass") || it.getName().equals("node_name") || it.getName().equals("edgeList") || it.getName().equals("proxyData"))
                    }
                    fields.find(it -> it.getAnnotation(Id.class)).with {
                        nodePrimary."${n.node_name}" = it.getName()
                    }
                    if(!f){
                        Map<String, Object> node = [label: n.node_name, type: 'VERTEX', detach_property: true, properties:[]]
                        //1.找出主键
                        java.lang.reflect.Field[] primary = fields.findAll {it.getAnnotation(Id.class)}
                        primary.each {it.setAccessible(true)}
                        if(primary.size() == 0){
                            throw new RuntimeException("primary key not found")
                        }
                        if(primary.size() > 1){
                            throw new RuntimeException("more than one primary key")
                        }
                        fields.each {
                            it.setAccessible(true)
                            String name = it.getName()
                            Boolean isIndex = null
                            Boolean isUnique = null
                            Boolean isOptional = false
                            Boolean exist = true
                            String type = ""
                            def a1 = it.getAnnotation(Id.class)
                            def a2 = it.getAnnotation(Field.class)
                            if(a2!=null){
                                if(a2.isIndex()){
                                    isIndex = true
                                }
                                if(a2.unique()){
                                    isUnique = true
                                }
                                isOptional = a2.optional()
                                exist = a2.exist()
                                type = a2.columnType()
                                if(type.trim().length() == 0){
                                    throw new RuntimeException("columnType not found")
                                }
                                if(a2.columnName().trim().length() != 0){
                                    name = a2.columnName()
                                }
                            }
                            if(a1!=null){
                                isIndex = true
                                isUnique = true
                                node.primary = name
                                if(!exist){
                                    throw new RuntimeException("primary key exist = false condition mutex")
                                }
                            }
                            if(!exist){
                                return
                            }
                            def prop = [
                                    name: name,
                                    type: type,
                                    optional: isOptional,
                            ]
                            if(isIndex){
                                prop.index = true
                            }
                            if(isUnique){
                                prop.unique = true
                            }
                            node.properties << prop
                            nodePrimary."${n.node_name}" = node.primary
                        }
                        d.createVertexLabelByJson(name, JSONUtil.toJsonStr(node))
                    }

//                    d.cleanNodeData(name, n.node_name)
                    //整理点数据
                    Map<String, Object> data = [:]
                    fields.findAll {
                        it.setAccessible(true)
                        def annotation = it.getAnnotation(Field.class)
                        if(it.getAnnotation(Id.class)){
                            return true
                        }
                        if(annotation?.exist()){
                            return true
                        }
                        return false
                    }
                    fields.each {
                        if(it.getAnnotation(Id.class)){
                            def id =  it.get(n)
                            if(!id){
                                id = SnowFlakeWorker.idGenerate()
                                if(!it.getType().name.contains("String")){
                                    id = Long.parseLong(id)
                                }
                                it.set(n, id)
                            }
                            data."${it.getName()}" = id
                            n?.edgeList?.each {
                                it.self_expand.fromPrimaryId = id
                            }
                        }else {
                            if(it.getAnnotation(Field.class).exist()){
                                if(it.get(n)){
                                    data."${it.getName()}" = it.get(n)
                                }
                            }
                        }
                    }
//                    d.upsertNodeData(name, n.node_name, [data])
                    if(nodeData.containsKey(n.node_name)){
                        nodeData."$n.node_name" << data
                    }else {
                        nodeData."$n.node_name" = [data]
                    }
                    n?.edgeList?.each { Edge e ->
                        e.fromNode_name = n.node_name
                        if(!e.toNode_name || e.toNode_name.trim().length() == 0){
                            throw new RuntimeException("$n.node_name toNode_name not found")
                        }
                        if(!e.edge_name || e.edge_name.trim().length() == 0){
                            e.edge_name = e.fromNode_name + "_" + e.toNode_name
                        }
                        edgeList << e
                    }
                }

            }

            TuGraphDriver.doExecute { TuGraphDriver d ->
                edgeList?.each { Edge e ->
                    if(!e.edge_name){
                        e.edge_name = e.getClass().getName()
                    }
                    def f = d.isExistEdge(name, e.edge_name)
                    def clazz = e.getClass()
                    java.lang.reflect.Field[] fields = clazz.getFields() + clazz.getDeclaredFields()
                    fields = fields.findAll {
                        return !(it.getName().contains("\$") || it.getName().equals("metaClass") || it.getName().equals("edge_name") || it.getName().equals("toNode_name") || it.getName().equals("fromNode_name") || it.getName().equals("proxyData") || it.getName().equals("toBindNode") || it.getName().equals("self_expand"))
                    }
                    if(!f){
                        Map<String, Object> edge = [label: e.edge_name, type: 'EDGE', detach_property: true, properties:[], constraints:[[e.fromNode_name, e.toNode_name]]]
                        fields.each {
                            it.setAccessible(true)

                            String name = it.getName()
                            Boolean isIndex = null
                            Boolean isUnique = null
                            Boolean isOptional = false
                            Boolean exist = true
                            String type = ""

                            def a2 = it.getAnnotation(Field.class)
                            if(a2!=null){
                                if(a2.isIndex()){
                                    isIndex = true
                                }
                                if(a2.unique()){
                                    isUnique = true
                                }
                                isOptional = a2.optional()
                                exist = a2.exist()
                                type = a2.columnType()
                                if(type.trim().length() == 0){
                                    throw new RuntimeException("columnType not found")
                                }
                                if(a2.columnName().trim().length() != 0){
                                    name = a2.columnName()
                                }
                            }
                            if(!exist){
                                return
                            }
                            def prop = [
                                    name: name,
                                    type: type,
                                    optional: isOptional,
                            ]
                            if(isIndex){
                                prop.index = true
                            }
                            if(isUnique){
                                prop.unique = true
                            }
                            edge.properties << prop
                        }
                        d.createEdgeLabelByJson(name, JSONUtil.toJsonStr(edge))
                    }
//                    d.cleanEdgeData(name, e.edge_name)
                    //整理边数据
                    Map<String, Object> data = [:]
                    data.from_val = e?.self_expand?.fromPrimaryId
                    Node node = e?.toBindNode

                    if(node == null){
                        throw new RuntimeException("toBindNode not null")
                    }
                    java.lang.reflect.Field field = (node.class.getFields() + node.class.getDeclaredFields()).findAll{
                        return !(it.getName().contains("\$") || it.getName().equals("metaClass") || it.getName().equals("node_name") || it.getName().equals("edgeList") || it.getName().equals("proxyData"))
                    }.find {
                        it.isAnnotationPresent(Id.class)
                    }
                    if(field){
                        field.setAccessible(true)
                        data.to_val = field.get(node)
                    }

                    fields.each {
                        it.setAccessible(true)
                        if(it.getAnnotation(Field.class).exist()){
                            if(it.get(e)){
                                data."${it.getName()}" = it.get(e)
                            }
                        }
                    }
                    Map<String, List<Map<String, Object>>> belongMap = [:]
                    if(!edgeData.containsKey(e.edge_name)){
                        edgeData.put(e.edge_name, belongMap)
                    }else {
                        belongMap = edgeData.get(e.edge_name)
                    }
                    String key = e.fromNode_name +"@@"+ e.toNode_name
                    if(!belongMap.containsKey(key)){
                        belongMap."$key" = []
                    }
                    belongMap."$key" << data
                }
            }


            TuGraphDriver.doExecute { TuGraphDriver d ->
                nodeData?.each {k, v ->
                    d.cleanNodeData(name, k)
                    d.upsertNodeData(name, k, v)
                }
                edgeData?.each {k, v ->
                    d.cleanEdgeData(name, k)
                    v.each {k2, v2 ->
                        String[] orientation = k2.split("@@")
                        def keys= v2[0].keySet()
                        def props = []
                        keys.each {
                            if(it == "from_val" || it == "to_val"){
                                return
                            }
                            props << it
                        }
                        d.upsertEdgeData2(name, k, [[type: orientation[0], key: nodePrimary[orientation[0]]], [type: orientation[1], key: nodePrimary[orientation[1]]]], props,  v2)
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * 读取整图
     * @param graphName
     * @return
     */
    static Graph read(String graphName){
        JsonSlurper jsonSlurper = new JsonSlurper()
        def graph = new Graph(name: graphName, nodeList: [])
        def res = TuGraphDriver.doExecute{ TuGraphDriver d ->
            assert d.existsGraph(graphName) : "graph $graphName not found"
        }.getGraphSchema(graphName)
        if(res.hasNext()){
            def resStr = res.next().get(0).asString()

            def obj = jsonSlurper.parseText(resStr)
            def res2 = obj.schema.groupBy {
                it.type
            }
            TuGraphDriver.doExecute { TuGraphDriver d ->
                Map<String, Map<String, List<Edge>>> edgeData = [:]
                res2.each { k, v ->
                    switch (k){
                        case 'VERTEX':
                            v.each { n ->
                                graph.nodeList.addAll graph.getNode(n.label as String)
                            }
                            break
                        case 'EDGE':
                            v.each { e ->
                                if(!edgeData.containsKey(e.label)){
                                    edgeData."$e.label" = [:]
                                }
                                String key = e.constraints[0][0] +"@@"+ e.constraints[0][1]
                                if(!edgeData."$e.label".containsKey(key)){
                                    edgeData."$e.label"."$key" = []
                                }
                                def edges = graph.getEdge(e.label as String)
                                edges.each {
                                    it.fromNode_name = e.constraints[0][0]
                                    it.toNode_name = e.constraints[0][1]
                                }
                                edgeData."$e.label"."$key".addAll(edges)
                            }
                            break
                    }
                }

                def nodeMap = graph?.nodeList?.groupBy { it.node_name }
                edgeData?.each { k, v ->
                    v.each {
                        k2, v2 ->{
                            def relations = k2.split("@@")
                            if(nodeMap.containsKey(relations[0])){
                                nodeMap.get(relations[0])[0].edgeList.addAll v2
                            }
                        }
                    }
                }
            }
        }
        return graph
    }

    /**
     * 获取节点 数据
     * @param nodeName
     * @return
     */
    List<? extends Node> getNode(String nodeName, boolean isLoadEdge = false){
        assert name != null && name.length() > 0 : "graph name not found"
        assert nodeName != null && nodeName.length() > 0 : "nodeName not found"
        return TuGraphDriver.doExecute2 { TuGraphDriver d ->
            def resData = []
            def res = d.getVertexAllData(name, nodeName)
            while (res.hasNext()){
                def resNode = res.next().get(0).asNode()
                Node n = new Node() {{
                    proxyData = resNode.asMap()
                    node_name = nodeName
                }}
                if(isLoadEdge){
                    def resEdges = d.getInEdgeByVertex(name, nodeName)
                    while (resEdges.hasNext()){
                        def resEdge = resEdges.next().get(0).asString()
                        resEdge = resEdge.replace("[", "").replace("]", "")
                        n.edgeList.addAll getEdge(resEdge)
                    }
                }


                resData << n
            }
            return resData
        }

    }

    /**
     * 获取边数据
     * @param edgeName
     * @return
     */
    List<? extends Edge> getEdge(String edgeName){

        assert name != null && name.length() > 0 : "graph name not found"
        assert edgeName != null && edgeName.length() > 0 : "edgeName not found"
        return TuGraphDriver.doExecute2 { TuGraphDriver d ->
            def res = d.getEdgeAllData(name, edgeName)
            def schema = d.getEdgeSchema(edgeName)
            def map = schema.next().get(0).asMap()
            List<Edge> list = []
            while (res.hasNext()){
                def resEdge = res.next().get(0).asRelationship() as InternalRelationship
                Edge e = new Edge() {{
                    proxyData = resEdge.asMap()
                    edge_name = edgeName
                    fromNode_name = map.constraints[0][0]
                    toNode_name = map.constraints[0][1]
                }}
                list << e
            }
            return list
        }
    }


    /**
     * 以当前节点为准,向下查询所有点(包含自己)
     * @param nodeName
     * @return
     */
    List<? extends Node> getPositiveNodes(String nodeName){
        assert name != null && name.length() > 0 : "graph name not found"
        assert nodeName != null && nodeName.length() > 0 : "nodeName not found"
        return TuGraphDriver.doExecute2 { TuGraphDriver d ->
            List<Node> list = getNode(nodeName, true)
            def result = d.getPositiveNodes(name, nodeName)
            while (result.hasNext()){
                def tagetNodeName = result.next().get(0).asString()
                tagetNodeName = tagetNodeName.replace("[", "").replace("]", "")
                list.addAll getNode(tagetNodeName, true)
            }
            return list
        }
    }

    /**
     * 以当前节点为准,向上查询所有点（包含自己）
     * @param nodeName
     * @return
     */
    List<? extends Node> getNegativeNodes(String nodeName) {
        assert name != null && name.length() > 0 : "graph name not found"
        assert nodeName != null && nodeName.length() > 0 : "nodeName not found"
        return TuGraphDriver.doExecute2 { TuGraphDriver d ->
            List<Node> list = getNode(nodeName, true)
            def result = d.getNegativeNodes(name, nodeName)
            while (result.hasNext()) {
                def tagetNodeName = result.next().get(0).asString()
                tagetNodeName = tagetNodeName.replace("[", "").replace("]", "")
                list.addAll getNode(tagetNodeName, true)
            }
            return list
        }
    }


}
