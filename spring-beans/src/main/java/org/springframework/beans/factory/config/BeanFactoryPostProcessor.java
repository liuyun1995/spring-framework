package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

//Bean工厂后置加工器
public interface BeanFactoryPostProcessor {

	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
