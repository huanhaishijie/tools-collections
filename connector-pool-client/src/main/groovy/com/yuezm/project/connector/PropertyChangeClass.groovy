package com.yuezm.project.connector
/**
 * PropertyChangeClass
 * @description ${description}
 * @author yzm
 * @date 2024/6/4 14:06
 * @version 1.0
 */
abstract class PropertyChangeClass extends PropertyChangeAware implements GroovyInterceptable {

    PropertyChangeClass(){
        def clazz = this.metaClass
        this.metaClass = new MyMetaClass(clazz)
    }



    Object invokeMethod(String name, Object args) {
        boolean f = false
        def oldValue = null
        if(name.startsWith("set")){
            f = true
            String getMethodName = "get"+name.substring(3)
            oldValue = this.metaClass.getMetaMethod(getMethodName)?.invoke(this)
        }
        def result
        try {
            result = this.metaClass.getMetaMethod(name, args)?.invoke(this, args)
        }catch (Exception e){

        }

        if(f){
            this?.propertyChangeSupport.firePropertyChange(name, oldValue, args[0])
        }

        return result
    }
}
