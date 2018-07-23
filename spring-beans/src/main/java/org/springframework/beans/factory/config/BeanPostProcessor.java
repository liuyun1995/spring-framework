package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

//Bean定点加工器
public interface BeanPostProcessor {

	Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;

	Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;

}
