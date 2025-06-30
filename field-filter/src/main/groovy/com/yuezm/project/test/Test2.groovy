//package com.yuezm.project.test
//
//import com.yuezm.project.test.MyEdge
//import com.yuezm.project.test.MyNode
//import com.yuezm.project.tugraph.Graph
//import com.yuezm.project.tugraph.TuGraphConfig
//import com.yuezm.project.tugraph.TuGraphDriver
//
//def driver = new TuGraphDriver(hostUrl: '192.168.110.10', password: 'yuezhiming@Q123')
////1.注册图数据源
//TuGraphConfig.instance.register(driver)
////2.切换当前线程使用的图数据源
//TuGraphConfig.instance.getUseDriver()
//
////3.创建 man 节点 放入张三 李四 数据
//def zs = new MyNode(name: '张三', age: 18, node_name: 'man')
//def ls = new MyNode(name: '李四', age: 20, node_name: 'man')
//
////4.创建 woman 节点 放入李小红 王华 数据
//def lxh = new MyNode(name: '李小红', age: 20, node_name: 'woman')
//def ww = new MyNode(name: '王华', age: 22, node_name: 'woman')
//
////5.创建关系
//def edge1 = new MyEdge(edge_name: 'boy_friend', fromNode_name: 'man', toNode_name: 'woman', from: '张三', to: '李小红', relation: '男朋友', toBindNode: lxh)
//def edge2 = new MyEdge(edge_name: 'friend', fromNode_name: 'man', toNode_name: 'woman', from: '张三', to: '王华', relation: '朋友' , toBindNode: ww)
//zs.edgeList << edge1
//zs.edgeList << edge2
//def graph = new Graph(name: 'person_relation', nodeList: [zs, ls, lxh, ww])
//
////6.持久化图数据
//graph.persistence()
////7.读取整图数据
//def read = Graph.read('person_relation')
//
//
//
//
//
