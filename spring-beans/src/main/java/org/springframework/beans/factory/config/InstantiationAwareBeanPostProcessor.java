package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;

import java.beans.PropertyDescriptor;

public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

	Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException;

	boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException;

	PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException;

}
