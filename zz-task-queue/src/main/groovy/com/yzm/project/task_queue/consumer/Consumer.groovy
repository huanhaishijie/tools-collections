package com.yzm.project.task_queue.consumer

import com.yzm.project.task_queue.common.TaskState


/**
 * Consumer
 * @description ${description}
 * @author yzm
 * @date 2024/9/25 17:31
 * @version 1.0
 */
abstract class Consumer<T> {

    abstract TaskState consumer(T t)
}
