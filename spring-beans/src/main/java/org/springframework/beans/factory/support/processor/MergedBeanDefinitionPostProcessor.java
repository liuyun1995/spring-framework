package org.springframework.beans.factory.support.processor;

import org.springframework.beans.factory.bean.definition.RootBeanDefinition;

//合并的Bean定义加工器
public interface MergedBeanDefinitionPostProcessor extends BeanPostProcessor {

	void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName);

}
