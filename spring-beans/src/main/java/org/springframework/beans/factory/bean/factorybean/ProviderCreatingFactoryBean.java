package org.springframework.beans.factory.bean.factorybean;

import java.io.Serializable;
import javax.inject.Provider;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;

public class ProviderCreatingFactoryBean extends AbstractFactoryBean<Provider<Object>> {

    private String targetBeanName;

    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = targetBeanName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(this.targetBeanName, "Property 'targetBeanName' is required");
        super.afterPropertiesSet();
    }

    @Override
    public Class<?> getObjectType() {
        return Provider.class;
    }

    @Override
    protected Provider<Object> createInstance() {
        return new TargetBeanProvider(getBeanFactory(), this.targetBeanName);
    }

    @SuppressWarnings("serial")
    private static class TargetBeanProvider implements Provider<Object>, Serializable {

        private final BeanFactory beanFactory;

        private final String targetBeanName;

        public TargetBeanProvider(BeanFactory beanFactory, String targetBeanName) {
            this.beanFactory = beanFactory;
            this.targetBeanName = targetBeanName;
        }

        @Override
        public Object get() throws BeansException {
            return this.beanFactory.getBean(this.targetBeanName);
        }
    }

}
