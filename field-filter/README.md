# fieldFilter


### 转换脚本
[ParseCode.txt](src/main/resources/ParseCode.txt)

### 匹配规则介绍

1.案例数据
```angular2html
{
"memo": "行政区",
"field": dictProvince,
"fields": ["dictProvince", "dictCity", "dictCounty"],
"isArray": true,
"isRootArray": false,
"index": [0, 0, 0],
"type": ["val", "val", "val"],
"shell": "params.dictProvince + '/' + params.dictCity + '/' + params.dictCounty"
}

{
"memo": "分辨率",
"field": "dictRawResolution",
"isArray": true,
"type": "val",
"isRootArray": false,
"index": 0
}
```

2. 字段介绍
```angular2html
memo: 字段描述
field: 字段名 
fields 字段名列表 field和fields二选一
isArray: 是否有多个重名字段
isRootArray: 根对象是否是列表数据
index:多个重名字段取第几个 多个针对fields,坐标一一对应
shell: 需要拿到结果后再进行处理脚本
type: val 基本值 map 对象值 array 列表值
```


# tugraph

### 使用demo
1.1 构建图模型
```angular2html
/**
* MyEdge
*
* @author yzm
* @version 1.0
* @description 线模型结构
* @date 2025/6/27 20:02
*/
class MyEdge extends Edge {

@Field(columnType = "STRING")
String to

@Field(columnType = "STRING")
String from


@Field(columnType = "STRING")
String relation


}




/**
* MyNode
*
* @author yzm
* @version 1.0
* @description 点模型结构
* @date 2025/6/27 20:04
*/
class MyNode extends Node {

@Id
@Field(columnType = "STRING")
String id

@Field(columnType = "STRING")
String name

@Field(columnType = "INT8")
Integer age


}

```
1.2 构建图数据 创建图
```angular2html

def driver = new TuGraphDriver(hostUrl: '192.168.110.10', password: 'yuezhiming@Q123')
//1.注册图数据源
TuGraphConfig.instance.register(driver)
//2.切换当前线程使用的图数据源
TuGraphConfig.instance.getUseDriver()


//3.创建 man 节点 放入张三 李四 数据
def zs = new MyNode(name: '张三', age: 18, node_name: 'man')
def ls = new MyNode(name: '李四', age: 20, node_name: 'man')

//4.创建 woman 节点 放入李小红 王华 数据
def lxh = new MyNode(name: '李小红', age: 20, node_name: 'woman')
def ww = new MyNode(name: '王华', age: 22, node_name: 'woman')

//5.创建关系
def edge1 = new MyEdge(edge_name: 'boy_friend', fromNode_name: 'man', toNode_name: 'woman', from: '张三', to: '李小红', relation: '男朋友', toBindNode: lxh)
def edge2 = new MyEdge(edge_name: 'friend', fromNode_name: 'man', toNode_name: 'woman', from: '张三', to: '王华', relation: '朋友' , toBindNode: ww)
zs.edgeList << edge1
zs.edgeList << edge2
def graph = new Graph(name: 'person_relation', nodeList: [zs, ls, lxh, ww])

//6.持久化图数据
graph.persistence()

```
1.3 查询整图
```angular2html
def driver = new TuGraphDriver(hostUrl: '192.168.110.10', password: 'yuezhiming@Q123')
//1.注册图数据源
TuGraphConfig.instance.register(driver)
//2.切换当前线程使用的图数据源
TuGraphConfig.instance.getUseDriver()
//7.读取整图数据
def read = Graph.read('person_relation')
```
### 2.使用方法

```angular2html
def graph = Graph.read('person_relation')

(一)、驱动提供
1.创建图
graph.createGraph(String graphName)
//2.判断图是否存在
existsGraph(String graphName)
//3.删除图
deleteGraph(String graphName)
//4.选中图
checkedGraph(String graphName)
//5.删除所有的点边数据和图schema
dropGraph(String graphName)
//6.只删除所有点边数据, 保留图schema
dropAllVertex(String graphName)
//7.查看图schema
getGraphSchema(String graphName)
//8.列出所有子图
listGraphs()
//9.刷新子图文件系统缓存数据
flushDB(String graphName)
//10.创建点类型
createVertexLabelByJson(String graphName, String json)
//11.查看点类型
getVertexSchema(String graphName, String vertexName)
//12.删除点类型 (该操作会同步删除所有该类型的点数据，数据量大的时候，有时间消耗。)
deleteLabel(String dbName = null, String graphName)
//13.创建边类型
createEdgeLabelByJson(String graphName, String json)
//14.查看边类型
getEdgeSchema(String graphName, String edgeName)
//15.删除边类型
deleteEdgeLabel(String dbName = null, String graphName)
//16.判断点是否已经存在，存在返回true，不存在返回false
isExistPoint(String graphName, String vertexName)
//17.获取点所有数据
getVertexAllData(String graphName, String nodeName)
//18.判断边是否已经存在，存在返回true，不存在返回false
isExistEdge(String graphName, String edgeName)
//19.获取边所有数据
getEdgeAllData(String graphName, String edgeName)
//20.批量upsert点数据(相邻节点主键重名有问题)
upsertNodeData(String graphName ,String nodeName, List<T> datas)
//21.批量upsert边数据
    (String graphName, String edgeName, List<T> nodes, List<String> props, List<T> datas)
//22.清空节点数据 
cleanNodeData(String graphName ,String nodeName)
//23.批量upsert边数据
upsertEdgeData(String graphName, String edgeName, List<T> nodes, List<T> datas)
//24.清除边数据
cleanEdgeData(String graphName, String edgeName)
//25.根据当前节点，正向查询后面的所有节点
getPositiveNodes(String graphName, String edgeName)
//26.根据当前节点，反向查询前面的所有节点
getNegativeNodes(String graphName, String edgeName)


（二）、整合提供Graph
//1.整图支持化
persistence()
//2.读取整图数据
static Graph read(String graphName)
//3.获取节点 数据
List<? extends Node> getNode(String nodeName)
//4.获取边数据
List<? extends Edge> getEdge(String edgeName)
//5.以当前节点为准,向下查询所有点(包含自己)
List<? extends Node> getPositiveNodes(String nodeName)
//6.以当前节点为准,向上查询所有点(包含自己)
List<? extends Node> getNegativeNodes(String nodeName)

(三) 底层cypher开放
graph.driver.session().run("CALL db.dropDB()")         

```















