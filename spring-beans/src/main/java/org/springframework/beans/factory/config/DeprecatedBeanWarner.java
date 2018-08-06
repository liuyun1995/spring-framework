package org.springframework.beans.factory.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ConfigurableListableBeanFactory;
import org.springframework.beans.bean.definition.BeanDefinition;
import org.springframework.beans.support.processor.BeanFactoryPostProcessor;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public class DeprecatedBeanWarner implements BeanFactoryPostProcessor {

    protected transient Log logger = LogFactory.getLog(getClass());

    public void setLoggerName(String loggerName) {
        this.logger = LogFactory.getLog(loggerName);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (isLogEnabled()) {
            String[] beanNames = beanFactory.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                String nameToLookup = beanName;
                if (beanFactory.isFactoryBean(beanName)) {
                    nameToLookup = BeanFactory.FACTORY_BEAN_PREFIX + beanName;
                }
                Class<?> beanType = ClassUtils.getUserClass(beanFactory.getType(nameToLookup));
                if (beanType != null && beanType.isAnnotationPresent(Deprecated.class)) {
                    BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                    logDeprecatedBean(beanName, beanType, beanDefinition);
                }
            }
        }
    }

    protected void logDeprecatedBean(String beanName, Class<?> beanType, BeanDefinition beanDefinition) {
        StringBuilder builder = new StringBuilder();
        builder.append(beanType);
        builder.append(" ['");
        builder.append(beanName);
        builder.append('\'');
        String resourceDescription = beanDefinition.getResourceDescription();
        if (StringUtils.hasLength(resourceDescription)) {
            builder.append(" in ");
            builder.append(resourceDescription);
        }
        builder.append("] has been deprecated");
        writeToLog(builder.toString());
    }


    protected void writeToLog(String message) {
        logger.warn(message);
    }

    protected boolean isLogEnabled() {
        return logger.isWarnEnabled();
    }

}
