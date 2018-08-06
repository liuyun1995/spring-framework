package org.springframework.beans.bean.definition;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.support.processor.BeanFactoryPostProcessor;

//Bean定义注册器加工器
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;

}
