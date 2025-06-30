package com.yuezm.project.common


/**
 * FileUtil
 * @description ${description}
 * @author yzm
 * @date 2024/9/25 16:44
 * @version 1.0
 */
class FileUtil {
    static def createFileWithDirectories(String filePath) {
        def file = new File(filePath)
        createDirectoriesRecursively(file.parentFile)
        if(file.exists()){
            return file
        }
        if(file.createNewFile()){
            return file
        } else {
            throw new RuntimeException("创建文件失败")
        }
    }


    private static createDirectoriesRecursively(File dir) {
        if (!dir.exists()) {
            createDirectoriesRecursively(dir.parentFile)
            dir.mkdir()
        }
    }
}
