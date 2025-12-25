import com.mysql.cj.jdbc.result.ResultSetMetaData
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

String driver = "com.mysql.cj.jdbc.Driver"
String url = "jdbc:mysql://10.2.170.50:9030/dorisdb?useSSL=false&serverTimezone=Asia/Shanghai"
String username = "root"
String password = "doris_123456"

Class.forName(driver)
def connection = DriverManager.getConnection(url, username, password)
//def statement = connection.prepareStatement("select project_all_name projectAllName from jsk_project limit 1")
//def resultSet = statement.executeQuery()
//ResultSetMetaData metaData = resultSet.getMetaData()
//int columnCount = metaData.getColumnCount()
////for (int i = 1; i <= columnCount; i++) {
////    String columnName = metaData.getColumnName(i)  // 获取列名
////    String columnLabel = metaData.getColumnLabel(i) // 获取列的显示名称（如果有别名则返回别名）
////    println "列 ${i}: 名称=${columnName}, 显示名称=${columnLabel}"
////}




//println "3".matches("-?\\d+(\\.\\d+)?")