package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanPostProcessor;

//合并的Bean定义加工器
public interface MergedBeanDefinitionPostProcessor extends BeanPostProcessor {

	void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName);

}
