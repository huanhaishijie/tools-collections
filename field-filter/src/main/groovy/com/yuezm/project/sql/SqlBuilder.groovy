package com.yuezm.project.sql


/**
 * SqlBuilder
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/8/12 10:07
 */
class SqlBuilder extends Wrapper{

    private StringBuilder conditionBuilder = new StringBuilder(" where 1 = 1 ")
    private Map<String, Object> conditionVal = new LinkedHashMap<>()

    private List<String> other = new ArrayList<String>()


    private List<String> searchColumns = new ArrayList<String>()

    private List<String> tableNames = new ArrayList<String>()

    private boolean multipleTable = false



    String sql = ""

    @Delegate
    Wrapper wrapper

    protected SqlBuilder(Wrapper wrapper){
        this.wrapper = wrapper
    }

    SqlBuilder addSearchColumns(List<String> searchColumns){
        this.searchColumns.addAll searchColumns.collect { getColumn(it)}
        return this
    }

    SqlBuilder addSearchColumn(String... searchColumn){
        this.searchColumns.addAll searchColumn.collect { getColumn(it)}
        return this
    }


    SqlBuilder addTableNames(List<String> tableNames){
        this.tableNames.addAll tableNames
        return this
    }

    SqlBuilder addTableName(String... tableNames){
        this.tableNames.addAll tableNames
        return this
    }


    SqlBuilder isMultipleTable(boolean multipleTable){
        this.multipleTable = multipleTable
        return this
    }


    SqlBuilder conditionJoin(String key, Object value, ConditionBuilder conditionBuilder){
        String condition = conditionBuilder.buildCondition(conditionVal, key, value)
        conditionBuilder.buildCondition(conditionVal, key, value)
        this.conditionBuilder.append(condition)
        return this
    }


    SqlBuilder addConditionVal(String key, Object value){
        conditionVal.put(key, value)
        return this
    }


    SqlBuilder addOther(String other){
        this.other.add(other)
        return this
    }

    String getSql(){
        if(sql ){
            return sql
        }
        if(multipleTable){
            return sql
        }
        sql = "select ${searchColumns.join(',')} from ${tableNames[0]}"
        if(conditionVal.size() > 0){
            sql += " ${conditionBuilder.toString()}"
        }
        if(other.size() > 0){
            sql += " ${other.join(' ')}"
        }

        return sql
    }



















}
