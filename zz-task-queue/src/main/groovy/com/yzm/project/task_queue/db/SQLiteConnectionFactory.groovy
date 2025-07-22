package com.yzm.project.task_queue.db

import com.yzm.project.task_queue.common.FileUtil
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import java.sql.Connection


/**
 * SQLiteConnectionFactory
 * @description ${description}
 * @author yzm
 * @date 2024/9/25 16:38
 * @version 1.0
 */
class SQLiteConnectionFactory {

    public static String ITEM_NAME = ""
    private final String SQLite_PATH = "/orm/basic/yzm/task_queue.db"

    private final String SQLite_JDBC_URL = "jdbc:SQLite:".concat(SQLite_PATH)
    private static volatile HikariDataSource dataSource


    protected Connection getConnection() {
        def f = dataSource == null ? createDataSource() : ""
        def connection
        try {
            connection = dataSource.getConnection()
        }catch (Exception e){
            createDataSource()
            connection = dataSource.getConnection()
        }


        return connection
    }
    protected getSQLiteDataSource() {
        def f = dataSource == null ? createDataSource() : ""
        return dataSource
    }
    protected synchronized void createDataSource() {
        FileUtil.createFileWithDirectories(System.getProperty("user.home") +"/"+ ITEM_NAME + "/orm/basic/yzm/task_queue.db")
        HikariConfig config = new HikariConfig()
        config.setJdbcUrl(SQLite_JDBC_URL)
        config.setPoolName("SQLitePool")
        config.setAutoCommit(true)
        // 池中最小空闲连接数量
        config.setMinimumIdle(2)
        // 池中最大连接数量
        config.setMaximumPoolSize(32)
        dataSource = new HikariDataSource(config)
    }




}
