package org.springframework.beans.factory.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.support.PropertiesLoaderSupport;

//属性工厂Bean
public class PropertiesFactoryBean extends PropertiesLoaderSupport
        implements FactoryBean<Properties>, InitializingBean {

    private boolean singleton = true;

    private Properties singletonInstance;

    public final void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    @Override
    public final boolean isSingleton() {
        return this.singleton;
    }


    @Override
    public final void afterPropertiesSet() throws IOException {
        if (this.singleton) {
            this.singletonInstance = createProperties();
        }
    }

    @Override
    public final Properties getObject() throws IOException {
        if (this.singleton) {
            return this.singletonInstance;
        } else {
            return createProperties();
        }
    }

    @Override
    public Class<Properties> getObjectType() {
        return Properties.class;
    }

    protected Properties createProperties() throws IOException {
        return mergeProperties();
    }

}
