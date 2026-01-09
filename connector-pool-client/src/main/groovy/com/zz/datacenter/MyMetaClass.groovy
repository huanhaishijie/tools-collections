package com.zz.datacenter
/**
 * MyMetaClass
 * @description ${description}
 * @author yzm
 * @date 2024/6/5 15:24
 * @version 1.0
 */
class MyMetaClass implements MetaClass {
    @Delegate
    MetaClass metaClass

    MyMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass
    }


    @Override
    void setProperty(Class sender, Object receiver, String property, Object value, boolean isCallToSuper, boolean fromInsideClass) {
        def oldValue = getProperty(sender, receiver, property, isCallToSuper, fromInsideClass)
        metaClass.setProperty(sender, receiver, property, value, isCallToSuper, fromInsideClass)
        if(receiver instanceof PropertyChangeAware && oldValue != value){
            receiver?.propertyChangeSupport?.firePropertyChange(property, oldValue, value)
        }
    }

    @Override
    void setProperty(Object object, String property, Object newValue) {
        def oldValue = metaClass.getProperty(object, property)
        metaClass.setProperty(object, property, newValue)
        if(object instanceof PropertyChangeAware && oldValue != newValue) {
            object?.firePropertyChange(property, oldValue, newValue)
        }
    }

}
