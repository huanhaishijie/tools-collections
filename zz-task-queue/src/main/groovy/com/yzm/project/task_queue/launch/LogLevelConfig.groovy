package com.yzm.project.task_queue.launch

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.logging.LoggingSystem
import org.springframework.context.annotation.Configuration
import org.springframework.boot.logging.LogLevel;


/**
 * LogLevelConfig
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2025/7/29 17:09
 */

@Configuration
class LogLevelConfig implements ApplicationRunner {

    @Autowired
    private LoggingSystem loggingSystem;

    @Override
    void run(ApplicationArguments args) {
        loggingSystem.setLogLevel("groovy.sql", LogLevel.OFF)
    }
}