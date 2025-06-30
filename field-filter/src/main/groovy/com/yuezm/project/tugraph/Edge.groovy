package com.yuezm.project.tugraph
/**
 * Edge
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/6/27 11:07
 */

abstract class Edge implements GroovyInterceptable, Serializable{
    String edge_name
    String toNode_name
    String fromNode_name
    Node toBindNode


    transient protected Map<String, Object> self_expand = [:]

    transient Map<String, Object> proxyData


    <T extends Edge> T asType(Class<T> clazz) {
        assert clazz != null: "clazz is null"
        def instance = clazz.newInstance()
        instance.proxyData = this.proxyData
        instance.edge_name = this.edge_name
        instance.toNode_name = this.toNode_name
        instance.fromNode_name = this.fromNode_name
        instance.toBindNode = this.toBindNode
        try {
            proxyData.each {k,v ->
                try {
                    instance."${k}" = v
                }catch (Exception e){
                }
            }
        }catch (Exception e){
        }
        return instance
    }


    Object invokeMethod(String name, Object args) {
        def result
        if(name.startsWith("get")){
            try {
                result = this.metaClass.getMetaMethod(name)?.invoke(this, args)
                if(result){
                    return result
                }
                def fieldName = name.substring(3).toString()
                fieldName = fieldName[0].toLowerCase() + fieldName.substring(1)
                if(proxyData && proxyData.containsKey(fieldName)){
                    result = proxyData.get(fieldName)
                }
                String setMethodName = "set"+name.substring(3)
                this.metaClass.getMetaMethod(setMethodName)?.invoke(this, result)
            }catch (Exception e){
            }
        }
        return result
    }


    void setBindNode(Node node){
        this.toBindNode = node
        if(this.toNode_name == null){
            this.toNode_name = node.node_name
        }
    }
}
