package com.yuezm.project.sql

import org.codehaus.groovy.control.CompilerConfiguration

import java.util.concurrent.atomic.AtomicInteger

class SafeScript extends Script {
    @Override
    Object getProperty(String name) {
        try {
            return super.getProperty(name)
        } catch (MissingPropertyException e) {
            return null  // 未定义变量 → null
        }
    }

    @Override
    Object run() {
        return null
    }
}
/**
 * SqlTemplate
 *
 * @author yzm
 * @version 1.0
 * @description sql 模板
 * @date 2025/12/8 9:55
 */
class SqlTemplateEngine {

    static String parse(String template, Map<String, Object> params) {
        template = template.trim()
        if(template.startsWith("# !groovy")){
            template = template.replace "# !groovy", ""
            def config = new CompilerConfiguration()
            config.scriptBaseClass = SafeScript.name
            def shell = new GroovyShell(new Binding(params), config)
            def res = shell.evaluate(template)
            if(res instanceof Map){
                if(res.params instanceof Map){
                    params << res.params
                }
                if(res.sql instanceof String){
                    return res.sql
                }
            }
            return res as String
        }
        if(template.startsWith("# !xml")){
            template = template.replace "# !xml", ""
            def config = new CompilerConfiguration()
            config.scriptBaseClass = SafeScript.name
            def shell = new GroovyShell(new Binding(params), config)
            String processed = template.replaceAll(/<if test="(.*?)">([\s\S]*?)<\/if>/) { full, cond, body ->
                def ok = shell.evaluate(cond)
                return ok ? body : ''
            }
            AtomicInteger index = new AtomicInteger(0)
            def numberMap = [:]
            processed = processed.replaceAll(
                    /<foreach collection="(.*?)" item="(.*?)" open="(.*?)" separator="(.*?)" close="(.*?)">([\s\S]*?)<\/foreach>/
            ) { full, collName, itemName, open, sep, close, body ->
                def coll = params[collName]
                if (!(coll instanceof Collection) || coll.isEmpty()) return ""
                def parts = coll.collect { item ->
                    String key = ":${itemName}_${index.getAndIncrement()}"
                    numberMap[key] = item
                    key
                }
                return open + parts.join(sep) + close
            }
            if(numberMap.size() >  0){
                params << numberMap
                numberMap.clear()
            }

            return processed
        }
        return template
    }


//    def cc = """
//# !xml
//select * from table where 1=1
//<if test="minAge != null && minAge !='' ">
//    AND age >= #{minAge}
//</if>
//and cc1 in
//<foreach collection="list" item="item" open="(" separator="," close=")">
//    #{item}
//</foreach>
//"""
    static void main(String[] args) {
//    String sqlScript = """# !groovy
//    def sql = "select * from tablexx where 1=1"
//    def index = 0
//    def res = [:]
//    def listParams = [:]
//    if(list){
//      sql += " and id in (" +list.collect {
//        listParams["id_\${index}"] = it
//        ":id_\${index++}"
//
//      }.join(",") +")"
//    }
//    if(minAge){
//        sql += " and age >= #{minAge}"
//    }
//    res.sql= sql
//    res.params = listParams
//    res
//"""


            def cc = """
# !xml
select * from table where 1=1
<if test="minAge != null && minAge !='' ">
    AND age >= #{minAge}
</if>
and cc1 in
<foreach collection="list" item="item" open="(" separator="," close=")">
    #{item}
</foreach>
"""
        def params = [minAge:25, list:[2,3,4]]
        def sql = SqlTemplateEngine.parse(cc, params)
        println sql

    }
}


