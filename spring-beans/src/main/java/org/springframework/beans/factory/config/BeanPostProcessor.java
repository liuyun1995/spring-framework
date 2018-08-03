package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

//Bean后置加工器
public interface BeanPostProcessor {

	//初始化Bean之前执行
	Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;

	//初始化Bean之后执行
	Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;

}
