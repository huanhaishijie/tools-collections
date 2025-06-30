package com.yuezm.project.apisix

import com.yzm.project.apiproxy.core.OkHttpUtils
import groovy.json.JsonSlurper
import org.apache.groovy.util.Maps

class ApiSix {

    synchronized static Boolean createRoute(String apiPath, Long qps, Long timeout){
        validConfig()
        apiPath =  apiPath.replaceAll(/\{[^}]+\}/, '*')
        def map = ApiSixConfig.instance.getRoteSettingFile()
        if(map.containsKey(apiPath)){
            throw new RuntimeException("路由已存在")
        }
        def max = ApiSixConfig.instance.getRoteSettingFile().values().max()
        if(!max){
            max = 0
        }
        max++
        while (findRouteExist(max)){
            max++
        }
        def content = [
                uri: apiPath,
                upstream: [
                        type: "roundrobin",
                        nodes: (0..< ApiSixConfig.instance.upstreams.size()).collectEntries {
                            [ApiSixConfig.instance.upstreams[it], it]
                        }
                ],
                timeout: [
                        connect: timeout,
                        read: 120,
                        send: 120
                ],
                plugins:[
                        "limit-req":[
                                rate: qps,
                                burst: 0,
                                "rejected_code": 503,
                                "key_type": "var",
                                "key": "remote_addr"
                        ]
                ]
        ]
        def result = OkHttpUtils.builder().mapParamsCurl "-a", "$ApiSixConfig.instance.apiSixHost/apisix/admin/routes/$max",
        "-H", ["X-API-KEY":"$ApiSixConfig.instance.apiKey"],
        "-X", "PUT",
        "-d", content
        if(result?.code != "200"){
            return false
        }
        ApiSixConfig.instance.put(apiPath, max)
        return true
    }


    private static boolean findRouteExist(Integer version){
        def result = OkHttpUtils.builder().curl "-a", "$ApiSixConfig.instance.apiSixHost/apisix/admin/routes/$version",
        '-H', "X-API-KEY","$ApiSixConfig.instance.apiKey",
        '-X', "GET"

        if(result?.code != "200"){
            throw new RuntimeException("校验路由失败")
        }
        def data = new JsonSlurper().parseText(result.data)

        if(data?.message == "Key not found"){
            return false
        }


        return true
    }


    synchronized static Boolean deleteRoute(String apiPath){
        validConfig()
        apiPath =  apiPath.replaceAll(/\{[^}]+\}/, '*')
        def map = ApiSixConfig.instance.getRoteSettingFile()
        if(!map.containsKey(apiPath)){
            throw new RuntimeException("路由不存在")
        }
        def index = map[apiPath]
        def result = OkHttpUtils.builder().curl "-a", "$ApiSixConfig.instance.apiSixHost/apisix/admin/routes/$index?force=true",
        '-H', "X-API-KEY","$ApiSixConfig.instance.apiKey",
        '-X', "DELETE"


        if(result?.code != "200"){
            return false
        }
        ApiSixConfig.instance.remove(apiPath)
        return true
    }


    static void validConfig(){

        if(!ApiSixConfig.instance.apiKey){
            throw new Exception("apiKey is null")
        }
        if(!ApiSixConfig.instance.apiSixHost){
            throw new Exception("apiSixHost is null")
        }
        if(!ApiSixConfig.instance.upstreams && ApiSixConfig.instance.upstreams.size() == 0){
            throw new Exception("upstreams is null")
        }
    }


    static void main(String[] args) {
        def instance = ApiSixConfig.instance
        instance.apiKey = "yuezhimingyuezhiming123"
        instance.apiSixHost = "http://192.168.110.10:9180"
        instance.upstreams = ["192.168.110.10:26805"]
//        def f = ApiSix.createRoute"/serve/apiManage/detail", 10, 300
//        f = ApiSix.deleteRoute "/serve/apiManage/detail"
//        println f
        def route = findRouteExist(2)
        println route


    }
}
