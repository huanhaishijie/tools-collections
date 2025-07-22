package com.yzm.project.task_queue.common


/**
 * Priority
 * @description ${description}
 * @author yzm
 * @date 2024/9/26 13:36
 * @version 1.0
 */
enum Priority {
    LOW(1, "Low Priority"),
    MEDIUM(2, "Medium Priority"),
    HIGH(3, "High Priority"),;

    final int value
    final String description

    Priority(int value, String description) {
        this.value = value
        this.description = description
    }

    int getValue() {
        return value
    }

    String getDescription() {
        return description
    }

}