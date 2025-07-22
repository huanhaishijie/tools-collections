//package com.yuezm.project.test

//import com.yuezm.project.test.MyEdge
//import com.yuezm.project.test.MyNode


import com.yuezm.project.test.TableFieldMappingEdge
import com.yuezm.project.test.TableNode
import com.yuezm.project.tugraph.Edge
import com.yuezm.project.tugraph.Graph
import com.yuezm.project.tugraph.Node
import com.yuezm.project.tugraph.TuGraphConfig
import com.yuezm.project.tugraph.TuGraphDriver
import groovy.json.JsonSlurper

def driver = new TuGraphDriver(hostUrl: '192.168.110.10', password: 'yuezhiming@Q123')
//1.注册图数据源
TuGraphConfig.instance.register(driver)
//2.切换当前线程使用的图数据源
TuGraphConfig.instance.getUseDriver()

//Graph graph = new Graph(name: 'test')

//正向获取
//def nodes = graph.getPositiveNodes('test_data_b')
//def map = nodes.groupBy { it.node_name}
//println map
//负向获取
//def edges = graph.getNegativeNodes('test_data_b')
//def map2 = edges.groupBy { it.node_name }
//println map2

////3.删除边
//graph.deleteLabel('')
//
//def edge = new Edge(){
//    String role
//    {
//        edge_name = 'acted_in'
//        role = 'Trinity'
//    }
//}
//Graph graph2 = new Graph(name: 'Movie_508E')
//graph2.deleteEdge(edge)


//Graph graph3 = new Graph(name: 'default')
//def node = graph3.getNodeById('public_dwd_sky_yjkc_jsydbp_nzydk_jgtc_21623b41dfdae47fff6120ab35005dc3', "21623b41dfdae47fff6120ab35005dc3" )


def nodeleft1_1 = new TableNode(
        node_name: "nodeleft1_1",
        id: "nodeleft1_1",
        datasourceId: "xxxxx",
        datasourceCode: "xxxxx",
        datasourceName: "xxxxxx",
        datasourceType: "cccc",
        tableId: "tableId",
        tableName: "nodeleft1_1",
)

def nodeleft1_2 = new TableNode(
        node_name: "nodeleft1_2",
        id: "nodeleft1_2",
        datasourceId: "xxxxx",
        datasourceCode: "xxxxx",
        datasourceName: "xxxxxx",
        datasourceType: "cccc",
        tableId: "tableId",
        tableName: "nodeleft1_2",
)

def nodeleft2_1 = new TableNode(
        node_name: "nodeleft2_1",
        id: "nodeleft2_1",
        datasourceId: "xxxxx",
        datasourceCode: "xxxxx",
        datasourceName: "xxxxxx",
        datasourceType: "cccc",
        tableId: "tableId",
        tableName: "nodeleft2_1",
)

def nodeleft2_2 = new TableNode(
        node_name: "nodeleft2_2",
        id: "nodeleft2_2",
        datasourceId: "xxxxx",
        datasourceCode: "xxxxx",
        datasourceName: "xxxxxx",
        datasourceType: "cccc",
        tableId: "tableId",
        tableName: "nodeleft2_2",
)

def node_center = new TableNode(
        node_name: "node_center",
        id: "node_center",
        datasourceId: "xxxxx",
        datasourceCode: "xxxxx",
        datasourceName: "xxxxxx",
        datasourceType: "cccc",
        tableId: "tableId",
        tableName: "node_center",
)

def noderight1_1 = new TableNode(
        node_name: "noderight1_1",
        id: "noderight1_1",
        datasourceId: "xxxxx",
        datasourceCode: "xxxxx",
        datasourceName: "xxxxxx",
        datasourceType: "cccc",
        tableId: "tableId",
        tableName: "noderight1_1",
)

def noderight1_2 = new TableNode(
        node_name: "noderight1_2",
        id: "noderight1_2",
        datasourceId: "xxxxx",
        datasourceCode: "xxxxx",
        datasourceName: "xxxxxx",
        datasourceType: "cccc",
        tableId: "tableId",
        tableName: "noderight1_2",
)

def noderight2_1 = new TableNode(
        node_name: "noderight2_1",
        id: "noderight2_1",
        datasourceId: "xxxxx",
        datasourceCode: "xxxxx",
        datasourceName: "xxxxxx",
        datasourceType: "cccc",
        tableId: "tableId",
        tableName: "noderight2_1",
)

def noderight2_2 = new TableNode(
        node_name: "noderight2_2",
        id: "noderight2_2",
        datasourceId: "xxxxx",
        datasourceCode: "xxxxx",
        datasourceName: "xxxxxx",
        datasourceType: "cccc",
        tableId: "tableId",
        tableName: "noderight2_2",
)

nodeleft2_1.edgeList << new TableFieldMappingEdge(
        fromFieldName: "nodeleft2_1",
        toFieldName: "nodeleft1_1",
        relationType: "1",
        bindNode: nodeleft1_1,
        edge_name: "nodeleft2_1_nodeleft1_1"
)

nodeleft2_2.edgeList << new TableFieldMappingEdge(
        fromFieldName: "nodeleft2_2",
        toFieldName: "nodeleft1_2",
        relationType: "1",
        bindNode: nodeleft1_2,
        edge_name: "nodeleft2_2_nodeleft1_2"
)

nodeleft1_1.edgeList << new TableFieldMappingEdge(
        fromFieldName: "nodeleft1_1",
        toFieldName: "node_center",
        relationType: "1",
        bindNode: node_center,
        edge_name: "nodeleft1_1_node_center"
)

nodeleft1_2.edgeList << new TableFieldMappingEdge(
        fromFieldName: "nodeleft1_2",
        toFieldName: "node_center",
        relationType: "1",
        bindNode: node_center,
        edge_name: "nodeleft1_2_node_center"
)

node_center.edgeList << new TableFieldMappingEdge(
        fromFieldName: "node_center",
        toFieldName: "noderight1_1",
        relationType: "1",
        bindNode: noderight1_1,
        edge_name: "node_center_noderight1_1"
)

node_center.edgeList << new TableFieldMappingEdge(
        fromFieldName: "node_center",
        toFieldName: "noderight1_2",
        relationType: "1",
        bindNode: noderight1_2,
        edge_name: "node_center_noderight1_2"
)

noderight1_1.edgeList << new TableFieldMappingEdge(
        fromFieldName: "noderight1_1",
        toFieldName: "noderight2_1",
        relationType: "1",
        bindNode: noderight2_1,
        edge_name: "noderight1_1_noderight2_1"
)

noderight1_2.edgeList << new TableFieldMappingEdge(
        fromFieldName: "noderight1_2",
        toFieldName: "noderight2_2",
        relationType: "1",
        bindNode: noderight2_2,
        edge_name: "noderight1_2_noderight2_2"
)


Graph graph1 = new Graph(name: 'test2')

def nodes = [nodeleft1_1, nodeleft1_2, nodeleft2_1, nodeleft2_2, node_center, noderight1_1, noderight1_2, noderight2_1, noderight2_2]
graph1.nodeList = nodes
//graph1.persistence()

//
//nodes.each {
//    graph1.deleteNode(it)
//    graph1.cleanNodeData(graph1.name, it.node_name)
//    it.edgeList?.each {
//        it2 ->
//            graph1.cleanEdgeData(graph1.name, it2.edge_name)
//            graph1.deleteEdgeLabel(graph1.name, it2.edge_name)
//    }
//    graph1.deleteLabel(graph1.name, it.node_name)
//}

def schema = graph1.getGraphSchema("test2").next().get(0).asString()
def obj = new JsonSlurper().parseText(schema) as Map
def edges = obj.schema.findAll { it.type == 'EDGE' }.collect { it.constraints[0] as List }

def forwardGraph = [:].withDefault { [] }
def reverseGraph = [:].withDefault { [] }
edges.each { from, to ->
    forwardGraph[from] << to
    reverseGraph[to] << from
}
def start = "node_center"
def visited = [(0): [start]]
def leftVisited = [start] as Set
def rightVisited = [start] as Set
def step = 0

def leftFrontier = [start]
def rightFrontier = [start]

while (leftFrontier || rightFrontier) {
    step++
    def newLeftFrontier = []
    def newRightFrontier = []

    leftFrontier.each { node ->
        reverseGraph[node].each { neighbor ->
            if (!leftVisited.contains(neighbor)) {
                leftVisited << neighbor
                newLeftFrontier << neighbor
                visited[-step] = (visited[-step] ?: []) + neighbor
            }
        }
    }

    rightFrontier.each { node ->
        forwardGraph[node].each { neighbor ->
            if (!rightVisited.contains(neighbor)) {
                rightVisited << neighbor
                newRightFrontier << neighbor
                visited[step] = (visited[step] ?: []) + neighbor
            }
        }
    }

    leftFrontier = newLeftFrontier
    rightFrontier = newRightFrontier
}

println visited



























