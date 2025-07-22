package com.yzm.project.task_queue.task


/**
 * TaskEo
 * @description ${description}
 * @author yzm
 * @date 2024/9/25 17:21
 * @version 1.0
 */
class TaskInfo {
    Long id
    String taskName
    String state
    String taskInfo
    Integer priority
    String taskProgress
    String callbackInfo
    Date finishTime
    Date startTime
    Integer timeout
    Date createTime
}
