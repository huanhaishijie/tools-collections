package com.yuezm.project.fieldFilter

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.groovy.util.Maps

import java.lang.reflect.Field
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.function.Function


/**
 * FeildFilter
 * @description ${TODO}
 * @author yzm
 * @date 2025/4/29 10:32
 * @version 1.0
 */



class FieldFilter {

    private Object data

    private LinkedList<Map<String, Object>> stackVal = new LinkedList<>()
    private LinkedList<Map<String, Object>> stackVal2 = new LinkedList<>()
    private boolean  serialization = true

    static final GroovyShell shell = new GroovyShell()


    private Integer count = 0



    private def stringToClosure(String code, Map<String, Object> p){
        def parse = shell.evaluate(" { params -> $code } ")
        return parse(p)
    }
    private FieldFilter(Object data){
        this.data = data
    }

    static <T> FieldFilter builder(T data, boolean serialization = true){
        assert data != null : "data is null"
        if(data instanceof  String){
            data = data.toString().trim()
            assert data?.length() != 0 : "data length not is 0 "
            try {
                new JsonSlurper().parseText(data)
            }catch (Throwable e){
            }
        }
        boolean isPrimitiveType = data instanceof Boolean || data instanceof Character || data instanceof Integer ||
                data instanceof Long || data instanceof Double || data instanceof Float ||
                data instanceof Short || data instanceof Byte || data instanceof BigDecimal ||
                data instanceof BigInteger || data instanceof Date || data instanceof Calendar ||
                data instanceof Timestamp || data instanceof LocalDate || data instanceof LocalDateTime
        assert !isPrimitiveType : "data  type ${data.class.name}" + isPrimitiveType + " not support"
        def filter = new FieldFilter(data)
        filter.serialization = serialization
        filter.init()
        return filter
    }

    private void init(){
        Object dataBack = data
        dispose(dataBack)
        if(!serialization){
            return
        }
        if(!(data instanceof String)){
            try {
                data = JsonOutput.toJson(data)
            }catch (Exception e){
                return
            }

        }

        data = new JsonSlurper().parseText(new String(((String)data).getBytes("UTF-8")))
        if(data instanceof List){
            stackVal.add([type: FieldEnum.array, key: "root", val: data])
        }
        def tempRoot = [data]
        def recordRoots = []
        for(int i=0;i<tempRoot.size();i++){
            def root = tempRoot[i]
            if(root instanceof Map){
                root.each { k,v ->
                    if(v instanceof Map || v instanceof List){
                        recordRoots.add(v)
                        if(v instanceof List){
                            stackVal.add([type: FieldEnum.array, key: k, val: v])
                        }else {
                            stackVal.add([type: FieldEnum.map, key: k, val: v])
                        }
                    }else {
                        stackVal.add([type: FieldEnum.val, key: k, val: v])
                    }
                }
            }else if(root instanceof List){
                root.each { v ->
                    if(v instanceof Map || v instanceof List){
                        recordRoots.add(v)
                    }
                }
            }
            if(i == tempRoot.size() - 1 && recordRoots.size() > 0){
                tempRoot = recordRoots
                recordRoots = []
                i = -1
            }
        }
        dispose(dataBack)
    }

    private void dispose(Object dataBack){
        def fields = dataBack.getClass().getFields() + dataBack.getClass().getDeclaredFields()
        fields.each {
            try {
                it.setAccessible(true)
                def val = it.get(dataBack)
                def key = it.name
                boolean f = val instanceof Boolean || val instanceof Character || val instanceof Integer ||
                        val instanceof Long || val instanceof Double || dataBack instanceof Float ||
                        val instanceof Short || val instanceof Byte || val instanceof BigDecimal ||
                        val instanceof BigInteger || val instanceof Date || val instanceof Calendar ||
                        val instanceof Timestamp || val instanceof LocalDate || val instanceof LocalDateTime ||
                        val instanceof String
                if(f){
                    stackVal2.add([type: FieldEnum.val, key: key, val: val])
                }else if(val instanceof Map){
                    stackVal2.add([type: FieldEnum.map, key: key, val: val])
                }else if(val instanceof List){
                    stackVal2.add([type: FieldEnum.array, key: key, val: val])
                }else {
                    stackVal2.add([type: FieldEnum.obj, key: key, val: val])
                }
            }catch (Exception e){
            }
        }
        return
    }



    <T, R> R findConvert(String key, FieldEnum type,  Function<T, R> closure){
        assert closure != null : "closure is null"
        def val = stackVal.findAll {
            it.key == key && it.type == type
        }?.val
        if(!val){
            val = stackVal2.findAll {
                it.key == key && it.type == type
            }?.val
        }

        return closure.apply(val)
    }


    <R> R findConvert(String key, FieldEnum type,  String closure, def index = 0){
        assert closure != null : "closure is null"

        def val = stackVal.findAll {
            it.key == key && it.type == type
        }?.val
        if(!val){
            val = stackVal2.findAll {
                it.key == key && it.type == type
            }?.val
        }
        return stringToClosure(closure, Maps.of(key, val[index]) )
    }

    <R> R convert(String closure,  Map<String, Object> vals){
        assert closure != null : "closure is null"
        return (R)stringToClosure(closure, vals)
    }

    <R> List<R> find(String key, FieldEnum type){
        List<R> val = stackVal.findAll {
            it.key == key && it.type == type
        }?.val?.collect {(R) it}
        if(!val){
            val = stackVal2.findAll {
                it.key == key && it.type == type
            }?.val?.collect {(R) it}
        }
        return val
    }


    <R> R findConvert(String key, FieldEnum type,  Closure<R> closure){
        assert closure != null : "closure is null"
        def val = stackVal.findAll {
            it.key == key && it.type == type
        }?.val
        if(!val){
            val = stackVal2.findAll {
                it.key == key && it.type == type
            }?.val
        }

        return closure.call(val)
    }


}
