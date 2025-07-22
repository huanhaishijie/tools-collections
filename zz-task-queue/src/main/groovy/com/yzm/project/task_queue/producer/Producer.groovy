package com.yzm.project.task_queue.producer

import com.yzm.project.task_queue.common.JDKSerializable
import com.yzm.project.task_queue.common.Priority
import com.yzm.project.task_queue.common.SnowFlakeWorker
import com.yzm.project.task_queue.common.TaskState
import com.yzm.project.task_queue.db.GroovySqlConfig
import com.yzm.project.task_queue.task.TaskInfo
import groovy.sql.Sql


/**
 * Producer
 * @description ${description}
 * @author yzm
 * @date 2024/9/25 17:33
 * @version 1.0
 */
@Singleton
class Producer {

    <T> void send(String taskName, T task, Priority priority = Priority.MEDIUM) {
        Objects.requireNonNull(task, "信息体不能为空")
        if(taskName == null ||taskName?.isEmpty()){
            throw new RuntimeException("任务名称不能为空")
        }
        TaskInfo taskInfo = new TaskInfo().tap {
            id = SnowFlakeWorker.idGenerate() as Long
            it.taskName = taskName
            state = TaskState.ready
            taskInfo = JDKSerializable.serialize(task)
            it.priority = priority?.value
            it.createTime = new Date()
        }
        GroovySqlConfig.instance.doExecute{
            Sql sql ->
                String insertSql = "insert into sys_task(id, task_name, state, task_info, priority, create_time) values(?,?,?,?,?,?)"
                sql.execute(insertSql, taskInfo.id, taskInfo.taskName, taskInfo.state, taskInfo.taskInfo, taskInfo.priority, taskInfo.createTime)
        }
    }
}
