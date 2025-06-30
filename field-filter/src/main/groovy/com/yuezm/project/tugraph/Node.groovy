package com.yuezm.project.tugraph

import cn.hutool.json.JSONException
import cn.hutool.json.JSONObject


/**
 * Node
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/6/27 11:06
 */

abstract class Node implements GroovyInterceptable, Serializable{
    String node_name

    List<Edge> edgeList = []

    transient Map<String, Object> proxyData

    <T extends Node> T asType(Class<T> clazz) {
        assert clazz != null : "clazz is null"
        def instance = clazz.newInstance()
        instance.proxyData = this.proxyData
        instance.node_name = this.node_name
        instance.edgeList = this.edgeList
        try {
            proxyData.each {k,v ->
                instance."${k}" = v
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


}
