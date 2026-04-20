package com.yuezm.project.gbd


/**
 * GbdReader
 *
 * @author yzm
 * @version 1.0
 * @description ${TODO}
 * @date 2026/4/15 10:45
 */
class GbdReader {
    static List<String> getLayers(String gdalRoot, String gdbPath) {
        def cmd = ["docker-compose",  "run", "--rm",
                   "gdal", "ogrinfo", gdbPath]
        def pb = new ProcessBuilder(cmd)
        pb.directory(new File(gdalRoot))
        pb.redirectErrorStream(true)
        def process = pb.start()
        def output = process.inputStream.getText("UTF-8")
        process.waitFor()
        def layers = []
        output.eachLine { line ->
            if (line.startsWith("Layer:")) {
                def name = line.substring("Layer:".length()).trim()
                name = name.replaceAll(/\s*\(.*\)$/, "")
                layers << name
            }
        }
        return layers
    }
    static boolean toDatabase(String dsType = "PostgreSQL",
                              DbConfig dbConfig,
                              String gdalRoot,
                              String gdbPath, String layerName){
        def cmd = [
                "docker-compose", "run", "--rm",
                "gdal",
                "ogrinfo",
                "-f", dsType,
                "PG:host=$dbConfig.host port=$dbConfig.port dbname=$dbConfig.dbname user=$dbConfig.user password=$dbConfig.password".toString(),
                gdbPath,
                layerName
        ]
        if(dbConfig.schema){
            cmd << "-lco" << "SCHEMA=${dbConfig.schema}".toString()
        }
        def pb = new ProcessBuilder(cmd)
        pb.directory(new File(gdalRoot))
        pb.redirectErrorStream(true)
        def p = pb.start()
        def output = p.inputStream.getText("UTF-8")
        int code = p.waitFor()
        println output
        return code == 0
    }

    static void main(String[] args) {
        println getLayers("d:/gdal", "data.gdb")
        println getLayerSpaceColumns("d:/gdal", "data.gdb", "ods_gtkjghyzt_nbsgdjtxwgh_gdcd")
//        println toDatabase("PostgreSQL",
//                new DbConfig(
//                        host: "192.168.110.9",
//                        port: "25432",
//                        schema: "test",
//                        dbname: "hxstest",
//                        user: "postgres",
//                        password: "skzz@2023"),
//                "d:/gdal", "data.gdb", "ods_gtkjghyzt_nbsgdjtxwgh_gdcd")
    }

    static List<String> getLayerSpaceColumns(String gdalRoot, String gdbPath, String layerName) {

        def cmd = [
                "docker-compose", "run", "--rm",
                "gdal",
                "ogrinfo", "-so",
                gdbPath,
                layerName
        ]

        def pb = new ProcessBuilder(cmd)
        pb.directory(new File(gdalRoot))
        pb.redirectErrorStream(true)

        def p = pb.start()
        def output = p.inputStream.getText("UTF-8")
        int code = p.waitFor()
        println output

        if (code != 0) {
            throw new RuntimeException("ogrinfo 执行失败:\n" + output)
        }

        def geomColumns = []

        output.eachLine { line ->
            line = line.trim()

            if (line.startsWith("Geometry Column")) {
                def parts = line.split("=")
                if (parts.length > 1) {
                    geomColumns << parts[1].trim()
                }
            }

            if (line.toLowerCase().contains("geometry column")) {
                def parts = line.split("=")
                if (parts.length > 1) {
                    geomColumns << parts[1].trim()
                }
            }
        }

        return geomColumns.unique()
    }





}

class DbConfig {
    String dbType
    String host
    String port = "5432"
    String schema
    String dbname
    String user
    String password
}
