package org.springframework.beans.factory.support.processor;

import org.springframework.beans.bean.definition.RootBeanDefinition;

//合并Bean定义后置加工器
public interface MergedBeanDefinitionPostProcessor extends BeanPostProcessor {

	//合并Bean定义后执行操作
	void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName);

}
