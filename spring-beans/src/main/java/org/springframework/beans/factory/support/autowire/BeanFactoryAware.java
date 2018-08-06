package org.springframework.beans.factory.support.autowire;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.autowire.Aware;

//Bean工厂装配器
public interface BeanFactoryAware extends Aware {

	//设置Bean工厂
	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}
