package org.springframework.beans.factory.support.processor;

import org.springframework.beans.bean.registry.BeanDefinitionRegistry;
import org.springframework.beans.exception.BeansException;

//Bean定义注册器加工器
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;

}
