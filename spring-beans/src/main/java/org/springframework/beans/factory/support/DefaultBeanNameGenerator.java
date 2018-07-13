package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinition;

//Ĭ�ϵ�Bean����������
public class DefaultBeanNameGenerator implements BeanNameGenerator {

	@Override
	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		return BeanDefinitionReaderUtils.generateBeanName(definition, registry);
	}

}
