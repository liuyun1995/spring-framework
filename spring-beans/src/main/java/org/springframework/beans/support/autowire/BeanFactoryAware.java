package org.springframework.beans.support.autowire;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.BeanFactory;

//Bean工厂装配器
public interface BeanFactoryAware extends Aware {

	//设置Bean工厂
	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}
