package com.yuezm.project.tugraph


/**
 * TuGraphConfig
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/6/26 9:37
 */

@Singleton
class TuGraphConfig {

    private ThreadLocal<TuGraphDriver> useDriverThreadLocal = new ThreadLocal<TuGraphDriver>()


    Map<String, TuGraphDriver> useDriverMap = new HashMap<>(2, 0.75f)


    boolean register(String key = "default", TuGraphDriver driver){
        if(useDriverMap.containsKey(key)){
            println("TuGraphConfig register error, key: ${key} is exist")
            return false
        }
        useDriverMap.put(key, driver)
    }

    boolean unregister(String key = "default"){
        if(!useDriverMap.containsKey(key)){
            println("TuGraphConfig unregister error, key: ${key} is not exist")
            return false
        }
        useDriverMap.remove(key)
    }


    synchronized TuGraphDriver getUseDriver(String key = "default"){
        assert useDriverMap.size() > 0 : "TuGraphConfig useDriverMap size is 0, please register first"
        if(useDriverThreadLocal.get()){
            return useDriverThreadLocal.get()
        }else {
            if(useDriverMap.containsKey(key)){
                useDriverThreadLocal.set(useDriverMap.get(key))
                return useDriverThreadLocal.get()
            }
        }
        println "without driver"
        return null
    }

    synchronized void clear(){
        useDriverThreadLocal.remove()
    }

}
