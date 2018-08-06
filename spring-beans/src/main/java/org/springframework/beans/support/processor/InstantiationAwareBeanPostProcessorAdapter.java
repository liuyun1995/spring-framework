package org.springframework.beans.support.processor;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import org.springframework.beans.exception.BeansException;
import org.springframework.beans.property.PropertyValues;

//实例化装配Bean的后置处理器适配器
public abstract class InstantiationAwareBeanPostProcessorAdapter implements SmartInstantiationAwareBeanPostProcessor {

	//预测Bean类型
	@Override
	public Class<?> predictBeanType(Class<?> beanClass, String beanName) {
		return null;
	}

	//确定候选的构造器
	@Override
	public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	//获取早期Bean引用
	@Override
	public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
		return bean;
	}

	//Bean实例化之前执行
	@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	//Bean实例化之后执行
	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		return true;
	}

	//执行属性值
	@Override
	public PropertyValues postProcessPropertyValues(
			PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {

		return pvs;
	}

	//Bean实例化之前执行
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	//Bean实例化之后执行
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
