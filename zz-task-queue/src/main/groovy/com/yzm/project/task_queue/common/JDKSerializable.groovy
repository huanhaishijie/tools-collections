package com.yzm.project.task_queue.common

import java.nio.charset.StandardCharsets


/**
 * JDKSerializable
 * @description ${description}
 * @author yzm
 * @date 2023/12/19 12:58
 * @version 1.0
 */
class JDKSerializable {

    static String serialize(Object obj) {
        Objects.requireNonNull(obj, "obj is null")
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream(baos)
            oos.writeObject(obj)
            oos.close()
            def res = baos.toString(StandardCharsets.ISO_8859_1.name())
            baos.close()
            return res
        }catch (Exception e){
            e.printStackTrace()
            println "序列化失败"
        }
    }

    static <T> T deserialize(String str) {
        Objects.requireNonNull(str, "str is null")
        if (str.isEmpty()) {
            return null
        }
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes(StandardCharsets.ISO_8859_1))
            ObjectInputStream ois = new ObjectInputStream(bais)
            T t = (T)ois.readObject()
            ois.close()
            bais.close()
            return t
        }catch (Exception e){
            e.printStackTrace()
            println "反序列化失败"
        }
    }

}
