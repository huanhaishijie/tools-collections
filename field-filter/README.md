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











