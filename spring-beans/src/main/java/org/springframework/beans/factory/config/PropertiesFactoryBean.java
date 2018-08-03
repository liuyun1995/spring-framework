package org.springframework.beans.factory.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.support.PropertiesLoaderSupport;

//工厂Bean(获取Properties)
public class PropertiesFactoryBean extends PropertiesLoaderSupport
        implements FactoryBean<Properties>, InitializingBean {

    private boolean singleton = true;        //是否是单例
    private Properties singletonInstance;    //Properties对象

    //设置是否是单例
    public final void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    //是否是单例
    @Override
    public final boolean isSingleton() {
        return this.singleton;
    }

    //属性设置之后执行
    @Override
    public final void afterPropertiesSet() throws IOException {
        if (this.singleton) {
            this.singletonInstance = createProperties();
        }
    }

    //获取对象
    @Override
    public final Properties getObject() throws IOException {
        if (this.singleton) {
            return this.singletonInstance;
        } else {
            return createProperties();
        }
    }

    //获取对象类型
    @Override
    public Class<Properties> getObjectType() {
        return Properties.class;
    }

    //创建Properties对象
    protected Properties createProperties() throws IOException {
        return mergeProperties();
    }

}
