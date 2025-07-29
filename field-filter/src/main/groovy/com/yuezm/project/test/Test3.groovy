
import com.yuezm.project.tugraph.*

class BaseNode extends Node{

    @Id
    @Field(columnType = "STRING")
    String id

}

class MyNode2 extends BaseNode{

    @Field(columnType = "STRING")
    String name
}


def driver = new TuGraphDriver(hostUrl: '192.168.110.10', password: 'yuezhiming@Q123')
//1.注册图数据源
TuGraphConfig.instance.register(driver)
//2.切换当前线程使用的图数据源
TuGraphConfig.instance.getUseDriver()


def persistence = new Graph(name: "test333", nodeList: [new MyNode2(name: 'zhangsan')]).persistence()
println persistence




