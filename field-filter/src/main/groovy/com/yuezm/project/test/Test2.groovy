//package com.yuezm.project.test

//import com.yuezm.project.test.MyEdge
//import com.yuezm.project.test.MyNode

import com.yuezm.project.tugraph.Edge
import com.yuezm.project.tugraph.Graph
import com.yuezm.project.tugraph.Node
import com.yuezm.project.tugraph.TuGraphConfig
import com.yuezm.project.tugraph.TuGraphDriver

def driver = new TuGraphDriver(hostUrl: '192.168.110.10', password: 'yuezhiming@Q123')
//1.注册图数据源
TuGraphConfig.instance.register(driver)
//2.切换当前线程使用的图数据源
TuGraphConfig.instance.getUseDriver()

Graph graph = new Graph(name: 'test')

//正向获取
//def nodes = graph.getPositiveNodes('test_data_b')
//def map = nodes.groupBy { it.node_name}
//println map
//负向获取
def edges = graph.getNegativeNodes('test_data_b' )
def map2 = edges.groupBy { it.node_name}
println map2

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




