package com.yuezm.project.connector

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport


/**
 * PropertyChangeAware
 * @description ${description}
 * @author yzm
 * @date 2024/6/4 14:05
 * @version 1.0
 */
abstract class PropertyChangeAware {

    @Delegate
    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this)



    void addPropertyChangeListener(String propertyName, Closure listener) {
        this.propertyChangeSupport.addPropertyChangeListener(propertyName, (PropertyChangeListener)listener)
    }

    void addPropertyChangeListener(Closure listener) {
        this.propertyChangeSupport.addPropertyChangeListener((PropertyChangeListener)listener)
    }
//
//
//    // 移除属性变化监听器
//    void removePropertyChangeListener(PropertyChangeListener listener) {
//        this.propertyChangeSupport.removePropertyChangeListener(listener)
//    }
    void removePropertyChangeListener(Closure listener){
        this.propertyChangeSupport.removePropertyChangeListener ((PropertyChangeListener)listener)
    }

    void removePropertyChangeListener(String propertyName, Closure listener){
        this.propertyChangeSupport.removePropertyChangeListener (propertyName, (PropertyChangeListener)listener)
    }




}
