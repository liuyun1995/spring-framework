package org.springframework.beans.factory.support.strategy;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.bean.definition.RootBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

//Bean实例化策略接口
public interface InstantiationStrategy {

	//Bean实例化方法
	Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner)
			throws BeansException;

	//Bean实例化方法
	Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner,
					   Constructor<?> ctor, Object... args) throws BeansException;

	//Bean实例化方法
	Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner,
                       Object factoryBean, Method factoryMethod, Object... args) throws BeansException;

}
