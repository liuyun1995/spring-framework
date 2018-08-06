package org.springframework.beans.factory.support.processor;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.ConfigurableListableBeanFactory;

//Bean工厂后置加工器
public interface BeanFactoryPostProcessor {

	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
