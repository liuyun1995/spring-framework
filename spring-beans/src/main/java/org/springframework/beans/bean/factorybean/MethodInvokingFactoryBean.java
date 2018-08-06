package org.springframework.beans.bean.factorybean;

import org.springframework.beans.factory.config.MethodInvokingBean;
import org.springframework.beans.exception.FactoryBeanNotInitializedException;

public class MethodInvokingFactoryBean extends MethodInvokingBean implements FactoryBean<Object> {

    private boolean singleton = true;
    private boolean initialized = false;
    private Object singletonObject;

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        prepare();
        if (this.singleton) {
            this.initialized = true;
            this.singletonObject = invokeWithTargetException();
        }
    }

    //获取对象
    @Override
    public Object getObject() throws Exception {
        if (this.singleton) {
            if (!this.initialized) {
                throw new FactoryBeanNotInitializedException();
            }
            // Singleton: return shared object.
            return this.singletonObject;
        } else {
            // Prototype: new object on each call.
            return invokeWithTargetException();
        }
    }

    //获取对象类型
    @Override
    public Class<?> getObjectType() {
        if (!isPrepared()) {
            //表明还未充分实例化，因此返回null
            return null;
        }
        return getPreparedMethod().getReturnType();
    }

    //是否是单例
    @Override
    public boolean isSingleton() {
        return this.singleton;
    }

}
