package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;

import java.beans.PropertyDescriptor;

//实例化装配Bean的后置处理器
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

	//实例化之前执行
	Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException;

	//实例化之后执行
	boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException;

	//处理属性值
	PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException;

}
