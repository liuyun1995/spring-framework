package org.springframework.beans.bean.factorybean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.exception.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.autowire.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.support.autowire.BeanNameAware;
import org.springframework.beans.factory.ConfigurableBeanFactory;
import org.springframework.util.StringUtils;

//属性路径工厂Bean
public class PropertyPathFactoryBean implements FactoryBean<Object>, BeanNameAware, BeanFactoryAware {

    private static final Log logger = LogFactory.getLog(PropertyPathFactoryBean.class);
    private BeanWrapper targetBeanWrapper;
    private String targetBeanName;
    private String propertyPath;
    private Class<?> resultType;
    private String beanName;
    private BeanFactory beanFactory;

    public void setTargetObject(Object targetObject) {
        this.targetBeanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(targetObject);
    }

    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = StringUtils.trimAllWhitespace(targetBeanName);
    }

    public void setPropertyPath(String propertyPath) {
        this.propertyPath = StringUtils.trimAllWhitespace(propertyPath);
    }

    public void setResultType(Class<?> resultType) {
        this.resultType = resultType;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = StringUtils.trimAllWhitespace(BeanFactoryUtils.originalBeanName(beanName));
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;

        if (this.targetBeanWrapper != null && this.targetBeanName != null) {
            throw new IllegalArgumentException("Specify either 'targetObject' or 'targetBeanName', not both");
        }

        if (this.targetBeanWrapper == null && this.targetBeanName == null) {
            if (this.propertyPath != null) {
                throw new IllegalArgumentException(
                        "Specify 'targetObject' or 'targetBeanName' in combination with 'propertyPath'");
            }

            // No other properties specified: check bean name.
            int dotIndex = this.beanName.indexOf('.');
            if (dotIndex == -1) {
                throw new IllegalArgumentException(
                        "Neither 'targetObject' nor 'targetBeanName' specified, and PropertyPathFactoryBean " +
                                "bean name '" + this.beanName + "' does not follow 'beanName.property' syntax");
            }
            this.targetBeanName = this.beanName.substring(0, dotIndex);
            this.propertyPath = this.beanName.substring(dotIndex + 1);
        } else if (this.propertyPath == null) {
            // either targetObject or targetBeanName specified
            throw new IllegalArgumentException("'propertyPath' is required");
        }

        if (this.targetBeanWrapper == null && this.beanFactory.isSingleton(this.targetBeanName)) {
            // Eagerly fetch singleton target bean, and determine result type.
            Object bean = this.beanFactory.getBean(this.targetBeanName);
            this.targetBeanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
            this.resultType = this.targetBeanWrapper.getPropertyType(this.propertyPath);
        }
    }

    @Override
    public Object getObject() throws BeansException {
        BeanWrapper target = this.targetBeanWrapper;
        if (target != null) {
            if (logger.isWarnEnabled() && this.targetBeanName != null &&
                    this.beanFactory instanceof ConfigurableBeanFactory &&
                    ((ConfigurableBeanFactory) this.beanFactory).isCurrentlyInCreation(this.targetBeanName)) {
                logger.warn("Target bean '" + this.targetBeanName + "' is still in creation due to a circular " +
                        "reference - obtained value for property '" + this.propertyPath + "' may be outdated!");
            }
        } else {
            // Fetch prototype target bean...
            Object bean = this.beanFactory.getBean(this.targetBeanName);
            target = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        }
        return target.getPropertyValue(this.propertyPath);
    }

    @Override
    public Class<?> getObjectType() {
        return this.resultType;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
