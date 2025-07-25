package com.yzm.project.task_queue.db

import groovy.sql.Sql



import java.util.concurrent.atomic.AtomicBoolean


/**
 * GroovySqlConfig
 * @description ${description}
 * @author yzm
 * @date 2023/12/1 9:49
 * @version 1.0
 */
@Singleton(lazy = true)
class GroovySqlConfig {

    private Sql sql

    private  SQLiteConnectionFactory sqliteConnectionFactory = new SQLiteConnectionFactory()
    AtomicBoolean isInit = new AtomicBoolean(false)

    static final AtomicBoolean replace = new AtomicBoolean(false)


    Sql getSql() {
        try {
            if(sql == null || sql.connection.isClosed() || replace.get()){
                sql = new Sql(sqliteConnectionFactory.getConnection())
                if(replace.get()){
                    replace.set(false)
                }
            }
            if(!isInit.get()){
                sqlInit()
                isInit.set(true)
            }
        }catch (Exception e){
            e.printStackTrace()
        }

        return sql
    }

    private static final def tablesIsExist = [
            'SELECT count(name) as total FROM sqlite_master WHERE type=\'table\' AND name=\'sys_task\'',
            'SELECT count(name) as total FROM sqlite_master WHERE type=\'table\' AND name==\'sys_task_lock\';'
    ]



    private static final def createTablesDDL = [
        "drop table if exists sys_task;",
        "CREATE TABLE sys_task (\n" +
                "  id INTEGER NOT NULL,                                   -- 唯一标识符\n" +
                "  priority INTEGER ,                                   -- 优先级\n" +
                "  task_name TEXT,                                        -- 任务名称\n" +
                "  state TEXT,                                            -- 任务状态\n" +
                "  task_progress TEXT,                                    -- 任务进度\n" +
                "  callback_info TEXT,                                    -- 任务回调\n" +
                "  finish_time DATETIME,                                  -- 完成时间\n" +
                "  start_time DATETIME,                                   -- 开始时间\n" +
                "  timeout INTEGER,                                       -- 任务超时时间 毫秒\n" +
                "  create_time DATETIME,                                  -- 创建时间\n" +
                "  update_user TEXT,                                      -- 更新人\n" +
                "  update_time DATETIME,                                  -- 更新时间\n" +
                "  create_user TEXT,                                      -- 创建人\n" +
                "  expansion TEXT,                                        -- 拓展数据\n" +
                "  is_deleted INTEGER DEFAULT 0,                          -- 是否删除 1是 0 否\n" +
                "  task_info TEXT,                                        -- 任务详情\n" +
                "  compute_resource TEXT                                  -- 计算资源\n" +
                ");" +
                "" +
                "-- 在脚本中添加注释说明字段含义;\n" +
                "-- 优先级: priority;\n" +
                "-- 任务名称: task_name;\n" +
                "-- 任务状态: state;\n" +
                "-- 任务进度: task_progress;\n" +
                "-- 任务回调: callback_info;\n" +
                "-- 完成时间: finish_time;\n" +
                "-- 开始时间: start_time;\n" +
                "-- 任务超时时间 毫秒: timeout;\n" +
                "-- 创建时间: create_time;\n" +
                "-- 更新人: update_user;\n" +
                "-- 更新时间: update_time;\n" +
                "-- 创建人: create_user;\n" +
                "-- 拓展数据: expansion;\n" +
                "-- 是否删除 1是 0 否: is_deleted;\n" +
                "-- 任务详情: task_info;\n" +
                "-- 计算资源: compute_resource;",

        "drop table if exists sys_task_success;",
        "CREATE TABLE sys_task_success (\n" +
                "  id INTEGER NOT NULL,                                   -- 唯一标识符\n" +
                "  priority INTEGER ,                                   -- 优先级\n" +
                "  task_name TEXT,                                        -- 任务名称\n" +
                "  state TEXT,                                            -- 任务状态\n" +
                "  task_progress TEXT,                                    -- 任务进度\n" +
                "  callback_info TEXT,                                    -- 任务回调\n" +
                "  finish_time DATETIME,                                  -- 完成时间\n" +
                "  start_time DATETIME,                                   -- 开始时间\n" +
                "  timeout INTEGER,                                       -- 任务超时时间 毫秒\n" +
                "  create_time DATETIME,                                  -- 创建时间\n" +
                "  update_user TEXT,                                      -- 更新人\n" +
                "  update_time DATETIME,                                  -- 更新时间\n" +
                "  create_user TEXT,                                      -- 创建人\n" +
                "  expansion TEXT,                                        -- 拓展数据\n" +
                "  is_deleted INTEGER DEFAULT 0,                          -- 是否删除 1是 0 否\n" +
                "  task_info TEXT,                                        -- 任务详情\n" +
                "  compute_resource TEXT                                  -- 计算资源\n" +
                ");" +
                "" +
                "-- 在脚本中添加注释说明字段含义;\n" +
                "-- 优先级: priority;\n" +
                "-- 任务名称: task_name;\n" +
                "-- 任务状态: state;\n" +
                "-- 任务进度: task_progress;\n" +
                "-- 任务回调: callback_info;\n" +
                "-- 完成时间: finish_time;\n" +
                "-- 开始时间: start_time;\n" +
                "-- 任务超时时间 毫秒: timeout;\n" +
                "-- 创建时间: create_time;\n" +
                "-- 更新人: update_user;\n" +
                "-- 更新时间: update_time;\n" +
                "-- 创建人: create_user;\n" +
                "-- 拓展数据: expansion;\n" +
                "-- 是否删除 1是 0 否: is_deleted;\n" +
                "-- 任务详情: task_info;\n" +
                "-- 计算资源: compute_resource;",

        "drop table if exists sys_task_fail;",
        "CREATE TABLE sys_task_fail (\n" +
                "  id INTEGER NOT NULL,                                   -- 唯一标识符\n" +
                "  priority INTEGER ,                                   -- 优先级\n" +
                "  task_name TEXT,                                        -- 任务名称\n" +
                "  state TEXT,                                            -- 任务状态\n" +
                "  task_progress TEXT,                                    -- 任务进度\n" +
                "  callback_info TEXT,                                    -- 任务回调\n" +
                "  finish_time DATETIME,                                  -- 完成时间\n" +
                "  start_time DATETIME,                                   -- 开始时间\n" +
                "  timeout INTEGER,                                       -- 任务超时时间 毫秒\n" +
                "  create_time DATETIME,                                  -- 创建时间\n" +
                "  update_user TEXT,                                      -- 更新人\n" +
                "  update_time DATETIME,                                  -- 更新时间\n" +
                "  create_user TEXT,                                      -- 创建人\n" +
                "  expansion TEXT,                                        -- 拓展数据\n" +
                "  is_deleted INTEGER DEFAULT 0,                          -- 是否删除 1是 0 否\n" +
                "  task_info TEXT,                                        -- 任务详情\n" +
                "  compute_resource TEXT                                  -- 计算资源\n" +
                ");" +
                "" +
                "-- 在脚本中添加注释说明字段含义;\n" +
                "-- 优先级: priority;\n" +
                "-- 任务名称: task_name;\n" +
                "-- 任务状态: state;\n" +
                "-- 任务进度: task_progress;\n" +
                "-- 任务回调: callback_info;\n" +
                "-- 完成时间: finish_time;\n" +
                "-- 开始时间: start_time;\n" +
                "-- 任务超时时间 毫秒: timeout;\n" +
                "-- 创建时间: create_time;\n" +
                "-- 更新人: update_user;\n" +
                "-- 更新时间: update_time;\n" +
                "-- 创建人: create_user;\n" +
                "-- 拓展数据: expansion;\n" +
                "-- 是否删除 1是 0 否: is_deleted;\n" +
                "-- 任务详情: task_info;\n" +
                "-- 计算资源: compute_resource;",




            "drop table if exists sys_task_lock;",
            "CREATE TABLE sys_task_lock (\n" +
                "  id INTEGER NOT NULL,                                    -- 唯一标识符\n" +
                "  task_id INTEGER NOT NULL,                               -- 任务id\n" +
                "  status TEXT NOT NULL,                                   -- 状态 lock 上锁\\ unlock 解锁\n" +
                "  valid_time DATETIME NOT NULL,                           -- 有效时间\n" +
                "  create_time DATETIME,                                   -- 创建时间\n" +
                "  update_user TEXT,                                       -- 更新人\n" +
                "  update_time DATETIME,                                   -- 更新时间\n" +
                "  create_user TEXT,                                       -- 创建人\n" +
                "  expansion TEXT,                                         -- 拓展数据\n" +
                "  is_deleted INTEGER DEFAULT 0                            -- 是否删除 1是 0 否\n" +
                ");\n" +
                "\n" +
                "-- 注释保留在脚本中\n" +
                "-- COMMENT ON COLUMN sys_task_lock.task_id IS '任务id';\n" +
                "-- COMMENT ON COLUMN sys_task_lock.status IS '状态 lock 上锁\\ unlock 解锁';\n" +
                "-- COMMENT ON COLUMN sys_task_lock.valid_time IS '有效时间';\n" +
                "-- COMMENT ON COLUMN sys_task_lock.create_time IS '创建时间';\n" +
                "-- COMMENT ON COLUMN sys_task_lock.update_user IS '更新人';\n" +
                "-- COMMENT ON COLUMN sys_task_lock.update_time IS '更新时间';\n" +
                "-- COMMENT ON COLUMN sys_task_lock.create_user IS '创建人';\n" +
                "-- COMMENT ON COLUMN sys_task_lock.expansion IS '拓展数据';\n" +
                "-- COMMENT ON COLUMN sys_task_lock.is_deleted IS '是否删除 1是 0 否';",

    ]

    def doQuery = { fun ->
        def res
        try {
            res = fun.call(this.getSql())
        }catch (Exception e){
            e.printStackTrace()
            replace.set(true)
        }

        return res
    }


    def doExecute = { fun ->
        def res
        try {
            Sql s = this.getSql()
            s.withTransaction {
                res = fun.call(s)
            }
        }catch (Exception e){
            e.printStackTrace()
            replace.set(true)
        }
        return res
    }



    //Sql初始化
    void sqlInit() {
        def sql = sqliteConnectionFactory?.with {
            new Sql(it.getConnection())
        }
        boolean f = false

        tablesIsExist.each {
            if(f) {
                return
            }
            f = sql.firstRow(it)?.total > 0
        }
        if(f){
            return
        }
        createTablesDDL.each {
            sql.execute(it)
        }

    }





}
