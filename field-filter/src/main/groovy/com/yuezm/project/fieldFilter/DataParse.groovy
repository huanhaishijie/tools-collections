package com.yuezm.project.fieldFilter


/**
 * DataParse
 * @description ${TODO}
 * @author yzm
 * @date 2025/4/29 15:38
 * @version 1.0
 */

abstract class DataParse {


    protected FieldFilter fieldFilter

    protected String rule

    protected String useRuleKey

    protected final GroovyShell shell = new GroovyShell()

    DataParse(Object data, String rule, String useRuleKey){
        this.fieldFilter = FieldFilter.builder(data)
        this.rule = rule
        this.useRuleKey = useRuleKey
    }

    abstract def parse()

    private def stringToClosure(String code){

        def binding = new Binding()
        binding.setVariable("fieldFilter", fieldFilter)
        binding.setVariable("rule", rule)
        binding.setVariable("useRuleKey", useRuleKey)
        binding.setVariable("FieldEnum", com.yuezm.project.fieldFilter.FieldEnum)
        binding.setVariable("JsonSlurper", groovy.json.JsonSlurper)

        def shell = new GroovyShell(binding)

        def script = """
                
                
                return { fieldFilter, rule, useRuleKey -> 
                    $code
                }
            """

        def closure = shell.evaluate(script)
        return closure(fieldFilter, rule, useRuleKey)
    }

    def parse(String code){
        def closure = stringToClosure(code)
    }
}
