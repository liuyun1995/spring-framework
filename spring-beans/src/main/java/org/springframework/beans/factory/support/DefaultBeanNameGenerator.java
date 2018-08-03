package org.springframework.beans.factory.support;

import org.springframework.beans.factory.bean.definition.BeanDefinition;
import org.springframework.beans.factory.bean.definition.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.bean.definition.BeanDefinitionRegistry;

//默认Bean名称生成器
public class DefaultBeanNameGenerator implements BeanNameGenerator {

	//生成Bean的名称
	@Override
	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		return BeanDefinitionReaderUtils.generateBeanName(definition, registry);
	}

}
