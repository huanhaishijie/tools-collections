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
//def sql =\"\"\"
//        SELECT
//        a.id AS id,
//        a.local_pid AS localPid,
//        a.project_name AS projectName,
//        a.province_project_no AS provinceProjectNo,
//        a.project_type AS projectType,
//        a.division AS division,
//        a.province AS province,
//        a.province_id AS provinceId,
//        a.city AS city,
//        a.city_id AS cityId,
//        a.county AS county,
//        a.county_id AS countyId,
//        a.address AS address,
//        a.location_x AS locationX,
//        a.location_y AS locationY,
//        a.approval_no AS approvalNo,
//        a.approval_level AS approvalLevel,
//        a.approval_depart AS approvalDepart,
//        a.approval_date AS approvalDate,
//        a.build_corp_name AS buildCorpName,
//        a.build_corp_name_id AS buildCorpNameId,
//        a.build_corp_code AS buildCorpCode,
//        a.build_plan_no AS buildPlanNo,
//        a.project_plan_no AS projectPlanNo,
//        a.fund_source AS fundSource,
//        a.national_percent_tage AS nationalPercentTage,
//        a.invest AS invest,
//        a.deal_invest AS dealInvest,
//        a.area AS area,
//        a.deal_area AS dealArea,
//        a.length AS length,
//        a.deal_length AS dealLength,
//        a.nature AS nature,
//        a.scale AS scale,
//        a.purpose AS purpose,
//        a.is_major AS isMajor,
//        a.plan_start_date AS planStartDate,
//        a.plan_end_date AS planEndDate,
//        a.energy_save_info AS energySaveInfo,
//        a.transfinite_info AS transfiniteInfo,
//        a.data_source AS dataSource,
//        a.data_level AS dataLevel,
//        a.invest_nature AS investNature,
//        a.url AS url,
//        a.is_history AS isHistory,
//        a.create_time AS createTime,
//        a.update_time AS updateTime
//        FROM
//        jsk_jabph_sky_project_v1 a
//        left join jsk_jabph_sky_project_corp_v1 b on a.id = b.pid
//        left join jsk_jabph_sky_project_index_data_v1 c on a.id = c.pid
//        left join jsk_jabph_sky_project_unit_v1 d on a.id = d.pid
//        left join jsk_jabph_sky_project_tender_v1 e on a.id = e.pid
//        where 1=1
//        \"\"\"
//def index = 0
//def res = [:]
//def listParams = [:]
//if(projectName){
//    sql = "\$sql and a.project_name like %#{projectName}%"
//}
//if(corpName){
//    sql = "\$sql and b.corp_name like %#{corpName}%"
//}
//if(buildCorpName){
//    sql = "\$sql and a.build_corp_name like %#{buildCorpName}%"
//}
//if(performanceType){
//    sql = "\$sql and c.project_type = #{performanceType}"
//}
//if(projectType){
//    sql = "\$sql and a.project_type = #{projectType}"
//}
//if(purpose){
//    sql = "\$sql and a.purpose = #{purpose}"
//}
//if(nature){
//    sql = "\$sql and a.nature = #{nature}"
//}
//if(structure){
//    sql = "\$sql and d.structure = #{structure}"
//}
//if(tenderWay){
//    sql = "\$sql and e.tender_way = #{tenderWay}"
//}
//if(provinceIds){
//sql += " and province_id in (" +provinceIds.split(',').collect {
//        listParams["id_\${index}"] = it
//        ":id_\${index++}"
//      }.join(" ,") +" )"
//}
//
//if(cityIds){
//sql += " and city_id in (" +cityIds.split(',').collect {
//        listParams["id_\${index}"] = it
//        ":id_\${index++}"
//      }.join(" ,") +" )"
//}
//
//if(countyIds){
//sql += " and county_id in (" +countyIds.split(',').collect {
//        listParams["id_\${index}"] = it
//        ":id_\${index++}"
//      }.join(" ,") +" )"
//}
//sql = "\$sql limit \$pageSize offset \$offset"
//
//res.sql= sql.toString()
//res.params = listParams
//return res
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
        def params = [minAge:25, list:'1,2,3',pageSize:10,offset:0]
        def sql = SqlTemplateEngine.parse( cc, params)
        println sql

    }
}


