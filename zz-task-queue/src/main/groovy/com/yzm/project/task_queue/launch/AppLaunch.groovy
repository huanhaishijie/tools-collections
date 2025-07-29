package com.yzm.project.task_queue.launch

import com.yzm.project.task_queue.common.JDKSerializable
import com.yzm.project.task_queue.common.TaskState
import com.yzm.project.task_queue.consumer.Consumer
import com.yzm.project.task_queue.db.GroovySqlConfig
import com.yzm.project.task_queue.db.SQLiteConnectionFactory
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.reflections.Reflections
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

import javax.annotation.Resource
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * AppLaunch
 * @description ${description}
 * @author yzm
 * @date 2024/9/26 14:01
 * @version 1.0
 */
@Component
@Slf4j
@Import([LogLevelConfig.class])
class AppLaunch {
    def executor = Executors.newScheduledThreadPool(2)
    synchronized final Set<Consumer> consumerSet = new HashSet<>(15)
    private final Map<String, Integer> countMap = new HashMap<>()
    private final Integer limit = 10

    @Resource
    Environment environment

    @EventListener
    void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("sqlite task_queue has been launched.")
        try {
            String itemName = environment.getProperty("spring.application.name");
            try {
                def port = environment.getProperty("server.port")
                itemName = "$itemName$port"
            } catch (Exception e) {

            }

            SQLiteConnectionFactory.ITEM_NAME = itemName


        } catch (Exception e) {
            println "获取项目名称失败"
        }


        // 提交任务到线程池，每隔2秒执行一次
        executor.scheduleAtFixedRate({
            if (consumerSet.isEmpty()) {
                Reflections reflections = new Reflections("com")
                def consumerClazzSet = reflections.getSubTypesOf(Consumer.class)
                if (consumerClazzSet.size() == 0) {
                    log.info("No customers were found！！！！！！！！！！！！！！！！")
                    return
                }
                def list = consumerClazzSet.collect {
                    try {
                        return it.newInstance()
                    } catch (Exception e) {
                        return null
                    }
                }.findAll {
                    it != null
                }
                if (list.size() == 0) {
                    log.info("No available customers were found！！！！！！！！！！！！！！！！")
                    return
                }
                consumerSet.addAll(list)
            }
            GroovySqlConfig.getInstance().doQuery {
                Sql sql ->
                    String querySql = "select * from sys_task where state = 'ready' or state = 'fail' order by create_time asc limit 100"
                    def rows = sql.rows(querySql)
                    if (rows.size() == 0) {
                        return
                    }
                    //优先级排序
                    rows = rows.sort { a, b ->
                        a.get("priority") <=> b.get("priority") ?: a.get("create_time") <=> b.get("create_time")
                    }
                    rows.each { row ->
                        def taskInfo = row.get("task_info")
                        def taskName = row.get("task_name")
                        def id = row.get("id")
                        log.info("消费任务：${taskName}，${taskInfo}")
                        try {
                            def task = JDKSerializable.deserialize(taskInfo)
                            def result = consumerSet.collect {
                                TaskState state = TaskState.fail
                                try {
                                    //jdk反序列化的对象能直接使用，但不能支持泛型引用
                                    def clazz = task.class
                                    def method = it.getClass().getMethods().find({ m -> m.name == "consumer" && m.parameterCount == 1 && m.parameterTypes[0].name == clazz.name })
                                    if (method == null) {
                                        return
                                    }
                                    def newTask = method.getParameterTypes()[0].newInstance()
                                    def clazz1 = newTask.class
                                    clazz.getMethods().findAll { cla ->
                                        cla.name.startsWith("get")
                                    }.each { c1m ->
                                        def newMethods = clazz1.getMethods().findAll { c2m ->
                                            c2m.name.startsWith("set")
                                        }
                                        def resultMethod = newMethods.find { m -> m.name == c1m.name.replace('get', 'set') }
                                        if (resultMethod && resultMethod.name != 'setMetaClass' && resultMethod.name != 'setProperty') {
                                            try {
                                                resultMethod.invoke(newTask, c1m.invoke(task))
                                            }catch (Exception e) {
                                                log.error("set ${c1m.name} error ${newTask}")
                                            }

                                        }
                                    }
                                    if (method != null) {
                                        state = method.invoke(it, newTask)
                                    } else {
                                        return
                                    }
                                } catch (Exception e) {
                                    log.info("消费任务失败：${taskName}，${taskInfo}")
                                    e.printStackTrace()
                                    state = TaskState.fail
                                }
                                return state
                            }.find {
                                it == TaskState.success
                            }
                            if (result == null) {
                                result = TaskState.fail
                            }
                            GroovySqlConfig.getInstance().doExecute {
                                Sql sql2 ->
                                    String updateSql = "update sys_task set state = ? where id = ?"
                                    sql2.execute(updateSql, result.toString(), id)
                                    if (result == TaskState.success) {
                                        String insertSql = "insert into sys_task_success(id, task_name, state, task_info, priority, create_time) values(?,?,?,?,?,?)"
                                        sql2.execute(insertSql, id, taskName, result.toString(), taskInfo, row.get("priority"), row.get("create_time"))
                                        sql2.execute("delete from sys_task where id = ?", id)
                                    } else if (result == TaskState.fail) {
                                        if (!countMap.containsKey(id)) {
                                            countMap[id] = 0
                                        } else {
                                            countMap[id]++
                                        }
                                        if (countMap[id] > limit) {
                                            String insertSql = "insert into sys_task_fail(id, task_name, state, task_info, priority, create_time) values(?,?,?,?,?,?)"
                                            sql2.execute(insertSql, id, taskName, result.toString(), taskInfo, row.get("priority"), row.get("create_time"))
                                            sql2.execute("delete from sys_task where id = ?", id)
                                            countMap.remove(id)
                                        }
                                    }
                            }
                        } catch (Exception e) {
                            e.printStackTrace()
                            log.info("反序列化失败")
                        }
                    }
            }
        }, 0, 5, TimeUnit.SECONDS)
    }

    @EventListener
    void handleContextClosed(ContextClosedEvent event) {
        try {
            executor.shutdown()
        } catch (Exception e) {
            executor.shutdown()
        }

    }
}
