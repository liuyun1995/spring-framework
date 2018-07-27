package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinition;

//Bean名称生成器
public interface BeanNameGenerator {

	//生成Bean的名称
	String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry);

}
