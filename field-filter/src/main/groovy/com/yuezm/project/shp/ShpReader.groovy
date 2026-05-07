package com.yuezm.project.shp

import com.yuezm.project.gbd.DbConfig


/**
 * ShpReader
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2026/5/7 10:25
 */
class ShpReader {


    static void main(String[] args) {
        println toDatabase(
                new DbConfig(
                        host: "192.168.111.244",
                        port: "63306",
                        schema: "",
                        dbType: "mysql",
                        dbname: "test",
                        user: "root",
                        password: "skzz@2021"),
                "d:/gdal", "data_shp/ods_gtkjghyzt_nbsgdjtxwgh_gdxl.shp", "ods_gtkjghyzt_nbsgdjtxwgh_gdxl")
    }


    static boolean toDatabase(DbConfig dbConfig,
                              String gdalRoot,
                              String shpPath, String layerName, String newTableName = null){

        if(dbConfig.dbType.toLowerCase() == "mysql"){
            return toMysqlDatabase(dbConfig, gdalRoot, shpPath, layerName, newTableName)
        }

        return false

    }


    private static boolean toMysqlDatabase(DbConfig dbConfig, String gdalRoot,
                                           String shpPath, String layerName, String newTableName = null) {
        if(newTableName == null){
            newTableName = layerName
        }
        def cmd = [
                "docker-compose", "run", "--rm",
                "gdal",
                "ogr2ogr",
                // 输出格式
                "-f", "MySQL",

                // 覆盖
                "-overwrite",

                // 修复几何
                "-makevalid",

                // 自动转 multi
                "-nlt", "PROMOTE_TO_MULTI",

                // 跳过坏数据
                "-skipfailures",

                // 批量提交
                "-gt", "65536",

                // 进度
                "-progress",

                // 指定 SRID
                "-a_srs", "EPSG:4326",

                // 图层名
                "-nln", newTableName,

                // layer config
                "-lco", "ENGINE=InnoDB",
                "-lco", "GEOMETRY_NAME=shape",
                "-lco", "SPATIAL_INDEX=FALSE",
                "-lco", "FID=id",
                // 中文
                "--config", "SHAPE_ENCODING", "UTF-8",
                "MYSQL:${dbConfig.dbname},host=${dbConfig.host},port=${dbConfig.port},user=${dbConfig.user},password=${dbConfig.password}".toString(),
                shpPath,
                layerName
        ]
        def pb = new ProcessBuilder(cmd)
        pb.directory(new File(gdalRoot))
        pb.redirectErrorStream(true)

        def p = pb.start()
        def output = p.inputStream.getText("UTF-8")
        int code = p.waitFor()
        println output
        return code == 0
    }

}