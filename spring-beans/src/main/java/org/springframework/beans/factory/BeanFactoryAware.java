package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

//Bean工厂装配
public interface BeanFactoryAware extends Aware {
	
	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}