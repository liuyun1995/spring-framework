package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

import java.lang.reflect.Constructor;

public interface SmartInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessor {

	//预测Bean的类型
	Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException;

	//确定使用的构造器
	Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException;

	//获取早期Bean引用
	Object getEarlyBeanReference(Object bean, String beanName) throws BeansException;

}
