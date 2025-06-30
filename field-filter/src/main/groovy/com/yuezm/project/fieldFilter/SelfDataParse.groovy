package com.yuezm.project.fieldFilter

import groovy.json.JsonSlurper


/**
 * SelfDataParse
 * @description ${TODO}
 * @author yzm
 * @date 2025/4/29 16:50
 * @version 1.0
 */

class SelfDataParse extends DataParse{



    private SelfDataParse(Object data, String rule, String useRuleKey) {
        super(data, rule, useRuleKey)
    }

    @Override
    def parse() {
        String code = "def resData = []\n" +
                "        try {\n" +
                "            def ruleList = JsonSlurper.newInstance().parseText(new String(rule.getBytes(\"UTF-8\")))\n" +
                "            def targetRule = ruleList.find{\n" +
                "                useRuleKey == it.type\n" +
                "            }\n" +
                "\n" +
                "            targetRule?.rules?.each { r ->\n" +
                "                resData <<[isTitle: true, label: r.title, value: null ]\n" +
                "                r?.fields?.each { f ->\n" +
                "                    def memo = f?.memo\n" +
                "                    def fields = f?.fields as ArrayList\n" +
                "                    def field = f?.field as String\n" +
                "                    def isArray = f?.isArray\n" +
                "                    def isRootArray = f?.isRootArray\n" +
                "                    def index = f?.index\n" +
                "                    def type = f?.type\n" +
                "                    def shell = f?.shell as String\n" +
                "                    def tempData = ''\n" +
                "                    if(isRootArray && index){\n" +
                "                        tempData = fieldFilter.find(\"root\", FieldEnum.array)[index]\n" +
                "                        resData << [isTitle: false, label: memo, value: tempData ]\n" +
                "                        return\n" +
                "                    }\n" +
                "                    if(fields && index instanceof List && type instanceof List){\n" +
                "                        tempData = [:]\n" +
                "                        0..fields.size()-1.each { i ->\n" +
                "                            def fieldEnum = FieldEnum.valueOf(type[i] as String)\n" +
                "                            def tempField = fields[i] as String\n" +
                "                            def tempIndex = index[i]\n" +
                "                            List<Map> tempRes = fieldFilter.find(tempField, fieldEnum)\n" +
                "                            if(tempRes && isArray){\n" +
                "                                tempData.put(tempField, tempRes.get(tempIndex))\n" +
                "                            }\n" +
                "                        }\n" +
                "                        if(shell && shell.trim() != '' && tempData.keySet().size() == fields.size()){\n" +
                "                            tempData = fieldFilter.convert(shell, tempData)\n" +
                "                        }else {\n" +
                "                            tempData = ''\n" +
                "                        }\n" +
                "\n" +
                "                        resData << [isTitle: false, label: memo, value: tempData ]\n" +
                "                        return\n" +
                "                    }\n" +
                "                    def fieldEnum = FieldEnum.valueOf(type)\n" +
                "\n" +
                "                    if(shell && shell.trim() != '' && !isArray){\n" +
                "                        tempData = fieldFilter.findConvert(field, fieldEnum, shell)\n" +
                "\n" +
                "                    }else if(shell && shell.trim() != '' && isArray){\n" +
                "                        tempData = fieldFilter.findConvert(field, fieldEnum){ res ->\n" +
                "                            def data = null\n" +
                "                            if(isArray && index){\n" +
                "                                data = res[index]\n" +
                "                            }else {\n" +
                "                                data = res[0]\n" +
                "                            }\n" +
                "                            println memo\n" +
                "                            return fieldFilter.convert(shell, new LinkedHashMap<String, Object>(){{\n" +
                "                                put(field, data)\n" +
                "                            }})\n" +
                "                        }\n" +
                "                    }else if(!(shell && shell.trim() != '')){\n" +
                "                        tempData = fieldFilter.findConvert(field, fieldEnum){ res ->\n" +
                "                            def data = null\n" +
                "                            if(isArray && index){\n" +
                "                                data = res[index]\n" +
                "                            }else {\n" +
                "                                data = res[0]\n" +
                "                            }\n" +
                "                            return data\n" +
                "                        }\n" +
                "                    }\n" +
                "                    resData << [isTitle: false, label: memo, value: tempData ]\n" +
                "                }\n" +
                "            }\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "        }catch (Exception e){\n" +
                "            e.printStackTrace()\n" +
                "        }\n" +
                "\n" +
                "        println resData\n" +
                "        return resData"
        return parse(code)
    }
//    @Override
//    def parse() {
//        def resData = []
//        try {
//            def ruleList = new JsonSlurper().parseText(new String(rule.getBytes("UTF-8")))
//            def targetRule = ruleList.find{
//                useRuleKey == it.type
//            }
//
//            targetRule?.rules?.each { r ->
//                resData <<[isTitle: true, label: r.title, value: null ]
//                r?.fields?.each { f ->
//                    def memo = f?.memo
//                    def fields = f?.fields as ArrayList
//                    def field = f?.field as String
//                    def isArray = f?.isArray
//                    def isRootArray = f?.isRootArray
//                    def index = f?.index
//                    def type = f?.type
//                    def shell = f?.shell as String
//                    def tempData = ''
//                    if(isRootArray && index){
//                        tempData = fieldFilter.find("root", FieldEnum.array)[index]
//                        resData << [isTitle: false, label: memo, value: tempData ]
//                        return
//                    }
//                    if(fields && index instanceof List && type instanceof List){
//                        tempData = [:]
//                        0..fields.size()-1.each { i ->
//                            def fieldEnum = FieldEnum.valueOf(type[i] as String)
//                            def tempField = fields[i] as String
//                            def tempIndex = index[i]
//                            List<Map> tempRes = fieldFilter.find(tempField, fieldEnum)
//                            if(tempRes && isArray){
//                                tempData.put(tempField, tempRes.get(tempIndex))
//                            }
//                        }
//                        if(shell && shell.trim() != '' && tempData.keySet().size() == fields.size()){
//                            tempData = fieldFilter.convert(shell, tempData)
//                        }else {
//                            tempData = ''
//                        }
//
//                        resData << [isTitle: false, label: memo, value: tempData ]
//                        return
//                    }
//                    def fieldEnum = FieldEnum.valueOf(type)
//
//                    if(shell && shell.trim() != '' && !isArray){
//                        tempData = fieldFilter.findConvert(field, fieldEnum, shell)
//
//                    }else if(shell && shell.trim() != '' && isArray){
//                        tempData = fieldFilter.findConvert(field, fieldEnum){ res ->
//                            def data = null
//                            if(isArray && index){
//                                data = res[index]
//                            }else {
//                                data = res[0]
//                            }
//                            println memo
//                            return fieldFilter.convert(shell, new LinkedHashMap<String, Object>(){{
//                                put(field, data)
//                            }})
//                        }
//                    }else if(!(shell && shell.trim() != '')){
//                        tempData = fieldFilter.findConvert(field, fieldEnum){ res ->
//                            def data = null
//                            if(isArray && index){
//                                data = res[index]
//                            }else {
//                                data = res[0]
//                            }
//                            return data
//                        }
//                    }
//                    resData << [isTitle: false, label: memo, value: tempData ]
//                }
//            }
//
//
//
//
//
//        }catch (Exception e){
//            e.printStackTrace()
//        }
//
//        println resData
//        return resData
//
//
//    }




    static void main(String[] args) {



        String data = "{\n" +
                "        \"id\": \"68f94a0645f745b3af33d5826d45dfdc\",\n" +
                "        \"dictName\": \"cccccccc\",\n" +
                "        \"createTime\": 1745373660000,\n" +
                "        \"creater\": \"yuezm\",\n" +
                "        \"createrId\": \"1112068743883653120\",\n" +
                "        \"updater\": \"yuezm\",\n" +
                "        \"dictMemo\": \"ccc\",\n" +
                "        \"dictCode\": \"D20250423-000001\",\n" +
                "        \"pid\": \"#\",\n" +
                "        \"nodeType\": 1,\n" +
                "        \"dictDataType\": 0,\n" +
                "        \"hasChildren\": 0,\n" +
                "        \"dataType\": 0,\n" +
                "        \"isTile\": 3,\n" +
                "        \"metaStatus\": 0,\n" +
                "        \"hasFolderChildren\": 0,\n" +
                "        \"isPublish\": 1,\n" +
                "        \"updateTime\": 1745373660000,\n" +
                "        \"status\": 1,\n" +
                "        \"metadata\": [\n" +
                "            {\n" +
                "                \"dictId\": \"68f94a0645f745b3af33d5826d45dfdc\",\n" +
                "                \"dictDataTime\": \"2025-04-22T16:00:00Z\",\n" +
                "                \"dictBandNum\": 3,\n" +
                "                \"dictPixelHeight\": 12544,\n" +
                "                \"dictPixelWeight\": 15872,\n" +
                "                \"dictProjection\": \"GEOGCS[\\\"WGS 84\\\",DATUM[\\\"WGS_1984\\\",SPHEROID[\\\"WGS 84\\\",6378137,298.257223563,AUTHORITY[\\\"EPSG\\\",\\\"7030\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"6326\\\"]],PRIMEM[\\\"Greenwich\\\",0,AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]],UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9122\\\"]],AXIS[\\\"Latitude\\\",NORTH],AXIS[\\\"Longitude\\\",EAST],AUTHORITY[\\\"EPSG\\\",\\\"4326\\\"]]\",\n" +
                "                \"dictDataType\": 1,\n" +
                "                \"dictGeoRange\": \"[119.8828125, 30.30029296875, 121.2451171875, 29.2236328125]\",\n" +
                "                \"dictGeoTransfer\": \"[119.8828125, 8.58306884765625E-5, 0.0, 30.30029296875, 0.0, -8.58306884765625E-5]\",\n" +
                "                \"filePath\": \"/home/workspace/nfs/nfs/raster/59af50b2eaa5434fa7f71e16014546e0/绍兴市.tif\",\n" +
                "                \"dictEpsg\": \"4326\",\n" +
                "                \"dictFileFolder\": \"/home/workspace/nfs/nfs/raster/59af50b2eaa5434fa7f71e16014546e0\",\n" +
                "                \"dictBestLevel\": 13,\n" +
                "                \"dictRawResolution\": 30.0\n" +
                "            }\n" +
                "        ],\n" +
                "        \"publishList\": [\n" +
                "            {\n" +
                "                \"id\": \"f78479467f7c4b75835d98fb209c62af\",\n" +
                "                \"dictId\": \"68f94a0645f745b3af33d5826d45dfdc\",\n" +
                "                \"dictCode\": \"D20250423-000001\",\n" +
                "                \"url\": \"http://192.168.110.252:10081/zz-mapServer-v30/proxy\",\n" +
                "                \"type\": \"2\",\n" +
                "                \"status\": 1,\n" +
                "                \"updateTime\": 1746669383000,\n" +
                "                \"createTime\": 1746669383000,\n" +
                "                \"workspace\": \"SSTD\",\n" +
                "                \"style\": \"{\\\"data\\\":[[-1,4,4,153],[0.35020299999999999,27,88,219],[0.57434549999999995,186,255,116],[1,5,110,10]],\\\"type\\\":1,\\\"alpha\\\":[[255,255,255],[255,255,255]]}\",\n" +
                "                \"dictName\": \"cccccccc\",\n" +
                "                \"layerName\": \"D20250423-000001-P0\",\n" +
                "                \"styleType\": 1\n" +
                "            }\n" +
                "        ],\n" +
                "        \"pname\": \"根目录\"\n" +
                "    }"
        String rule = new File("D:\\program files\\Git\\resp\\field-filter\\src\\main\\resources\\parseGisInfo.json").text


        def parse = new SelfDataParse(data, rule, "sstd").parse()

    }
}
