package com.yzm.project.task_queue.common

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


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


    static void deleteRecursively(String filePath) {
        Path pathToDelete = Paths.get(filePath)
        if(Files.exists(pathToDelete)){
            Files.walk(pathToDelete)
                    .sorted((p1, p2) -> p2.compareTo(p1)) // 先删除子文件夹，再删除父文件夹
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            System.err.println("Failed to delete " + p + ": " + e.getMessage());
                        }
                    })
        }
//        Files.delete(pathToDelete)
    }

}
