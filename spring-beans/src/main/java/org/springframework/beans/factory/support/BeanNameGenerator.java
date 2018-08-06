package org.springframework.beans.factory.support;

import org.springframework.beans.bean.definition.BeanDefinition;
import org.springframework.beans.bean.definition.BeanDefinitionRegistry;

//Bean名称生成器
public interface BeanNameGenerator {

	//生成Bean的名称
	String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry);

}
