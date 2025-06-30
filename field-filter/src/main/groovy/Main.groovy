import groovy.json.JsonOutput

//import groovy.json.JsonSlurper
//
//String data = "{\n" +
//        "    \"code\": 200,\n" +
//        "    \"msg\": \"操作成功\",\n" +
//        "    \"data\": {\n" +
//        "        \"id\": \"98d88e95d7e64a63a3cefd65f370faa5\",\n" +
//        "        \"dictName\": \"测试-绍兴\",\n" +
//        "        \"createTime\": 1745725140000,\n" +
//        "        \"creater\": \"dap\",\n" +
//        "        \"createrId\": \"1029063087392555008\",\n" +
//        "        \"updater\": \"dap\",\n" +
//        "        \"dictCode\": \"D20250427-000002\",\n" +
//        "        \"pid\": \"2dbe6baecd0f4c519c07b9fe35974155\",\n" +
//        "        \"nodeType\": 1,\n" +
//        "        \"dictDataType\": 0,\n" +
//        "        \"hasChildren\": 0,\n" +
//        "        \"dataType\": 0,\n" +
//        "        \"isTile\": 2,\n" +
//        "        \"metaStatus\": 1,\n" +
//        "        \"hasFolderChildren\": 0,\n" +
//        "        \"isPublish\": 0,\n" +
//        "        \"updateTime\": 1745731380000,\n" +
//        "        \"status\": 1,\n" +
//        "        \"metadata\": [\n" +
//        "            {\n" +
//        "                \"dictId\": \"98d88e95d7e64a63a3cefd65f370faa5\",\n" +
//        "                \"dictDataTime\": \"2025-04-26T16:00:00Z\",\n" +
//        "                \"dictBandNum\": 3,\n" +
//        "                \"dictPixelHeight\": 12544,\n" +
//        "                \"dictPixelWeight\": 15872,\n" +
//        "                \"dictInformation\": \"[[0.0, 255.0, 154.67570263559367, 99.8869302238698], [0.0, 255.0, 162.60826820539282, 91.01124558620813], [0.0, 255.0, 154.6931359075731, 98.12583602887784]]\",\n" +
//        "                \"dictProjection\": \"GEOGCS[\\\"WGS 84\\\",DATUM[\\\"WGS_1984\\\",SPHEROID[\\\"WGS 84\\\",6378137,298.257223563,AUTHORITY[\\\"EPSG\\\",\\\"7030\\\"]],AUTHORITY[\\\"EPSG\\\",\\\"6326\\\"]],PRIMEM[\\\"Greenwich\\\",0],UNIT[\\\"degree\\\",0.0174532925199433,AUTHORITY[\\\"EPSG\\\",\\\"9122\\\"]],AXIS[\\\"Latitude\\\",NORTH],AXIS[\\\"Longitude\\\",EAST],AUTHORITY[\\\"EPSG\\\",\\\"4326\\\"]]\",\n" +
//        "                \"dictDataType\": 1,\n" +
//        "                \"dictGeoRange\": \"[119.8828125, 30.30029296875, 121.2451171875, 29.2236328125]\",\n" +
//        "                \"dictGeoTransfer\": \"[119.8828125, 8.58306884765625E-5, 0.0, 30.30029296875, 0.0, -8.58306884765625E-5]\",\n" +
//        "                \"filePath\": \"/home/workspace/zzts-file/nfs/raster/17ef910843e34f4990d0815dd4b76fe3/绍兴市.tif\",\n" +
//        "                \"dictEpsg\": \"4326\",\n" +
//        "                \"dictFileFolder\": \"/home/workspace/zzts-file/nfs/raster/17ef910843e34f4990d0815dd4b76fe3\",\n" +
//        "                \"dictBandName\": \"band1,band2,band3\",\n" +
//        "                \"dictBestLevel\": 14,\n" +
//        "                \"dictRawResolution\": 9.544\n" +
//        "            }\n" +
//        "        ],\n" +
//        "        \"pname\": \"自定义胶囊目录\"\n" +
//        "    },\n" +
//        "    \"success\": true\n" +
//        "}"
//
////是否切片 -1 切片失败 0否  1 切片准备 2切片完成 3切片中 4取消5暂停
//String code = "def res = ''\n" +
//        "switch (params.isTile){\n" +
//        "    case -1:\n" +
//        "        res = '切片失败'\n" +
//        "        break\n" +
//        "    case 0:\n" +
//        "        res = '否'\n" +
//        "        break\n" +
//        "    case 1:\n" +
//        "        res = '切片准备'\n" +
//        "        break\n" +
//        "    case 2:\n" +
//        "        res = '切片完成'\n" +
//        "        break\n" +
//        "    case 3:\n" +
//        "        res = '切片中'\n" +
//        "        break\n" +
//        "    case 4:\n" +
//        "        res = '取消'\n" +
//        "        break\n" +
//        "    case 5:\n" +
//        "        res = '暂停'\n" +
//        "        break\n" +
//        "    default:\n" +
//        "        break\n" +
//        "}\n" +
//        "return res"
//GroovyShell shell = new GroovyShell()
//def parse = shell.evaluate("{ params -> $code }")
//def res = parse([isTile:2])
//println res;  // 输出: 栅格
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//

def basicInfo = [
    name: "测试api",
    serverProtocol:'http',
    serverHost:'127.0.0.1',
    apiPath:'/api',

    requestMethod:'POST',
    apiAuth: [
            authWay: '2',
            authType: '1',
            authAddLocation: '1',
            params:[
                    key1:'va1'
            ]
    ],
    description: '描述',
]


def paramsConfig = [
        datasourceType: "POSTGRES",
        datasourceId: "1899387609280712705",
        datasourceName: "test",
        tables:["test1"],
        model:'2',
        sqlScript: ' select  name::varchar as alias1, age::int as age, id::varchar as id from test1   where 1=1  and id >= ${id}    group by id order by id desc',
        sqlRule:[
                checkedReqParams:[
                        [
                                paramName:"id",
                                paramType:"varchar",
                                required: true,
                                defaultValue: "1"
                        ]
                ],
                bodyParams:[],
                headerParams:[]
        ],
        defaultRule:[
                checkedResTableColumns: [
                        [
                               "colName": "name",
                                "aliasName": "alias1",
                                "dataType": "varchar",
                                "func": null,
                        ],
                        [
                                "colName": "age",
                                "aliasName": "age",
                                "dataType": "int",
                                "func": null,
                        ],
                        [
                                "colName": "id",
                                "aliasName": "id",
                                "dataType": "varchar",
                                "func": null,
                        ]
                ],
                checkedReqParams:[
                        queryParams:[
                                [
                                        colName:"id",
                                        paramName:"id",
                                        paramType:"varchar",
                                        condition:">=",
                                        required: "true",
                                        defaultValue:"1",
                                        desc:"col1 val"
                                ]
                        ],
//                        bodyParams:[
//                                [
//                                        colName: "col1",
//                                        paramName: "params1",
//                                        paramType: "int",
//                                        required: "true",
//                                        condition: "=",
//                                        defaultValue: "1",
//                                        desc: "col1 val"
//                                ]
//                        ],
//                        headerParams:[
//                                [
//                                        keyName: "key1",
//                                        keyValue: "val1",
//                                        keyDesc: "header1 val"
//                                ]
//                        ]

                ],
                openOtherParams: true,
                pageParams: [
                        [
                                paramName: "pageSize",
                                paramValue: "2",
                                desc: "每页条数"
                        ],[
                                paramName: "pageNum",
                                paramValue: "2",
                                desc: "当前页数"
                        ]
                ],
                sortParams: [
                        [
                                colName: "id",
                                dataType: "varchar",
                                sortType: "desc"
                        ]
                ],
                groupParams: [
                        [
                                colName: "id",
                                dataType: "varchar"
                        ]
                ]

        ]


]
