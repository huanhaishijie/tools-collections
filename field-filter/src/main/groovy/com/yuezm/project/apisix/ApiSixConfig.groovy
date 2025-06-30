package com.yuezm.project.apisix

import com.yuezm.project.common.FileUtil
import groovy.json.JsonOutput
import groovy.json.JsonSlurper


@Singleton
class ApiSixConfig {

    private JsonSlurper jsonSlurper = new JsonSlurper()

    private static final String routeSetting = System.getProperty("user.home").concat("/apisix/routes/setting.json")

    String apiKey

    String apiSixHost

    List<String> serveUrls

    List<String> upstreams

    private Map<String, Integer> routeMap

    protected Map<String, Integer> getRoteSettingFile() {
        def file = FileUtil.createFileWithDirectories(routeSetting)
        if(!file.text){
            file.text = "{}"
        }
        routeMap = jsonSlurper.parseText(new String(((String) file.text).getBytes("UTF-8")))
        return routeMap
    }

    synchronized protected void put(String key, Integer value) {
        routeMap.put(key, value)
        def file = FileUtil.createFileWithDirectories(routeSetting)
        file.text = JsonOutput.toJson(routeMap)
    }

    synchronized protected void remove(String key) {
        routeMap.remove(key)
        def file = FileUtil.createFileWithDirectories(routeSetting)
        file.text = JsonOutput.toJson(routeMap)
    }






}
