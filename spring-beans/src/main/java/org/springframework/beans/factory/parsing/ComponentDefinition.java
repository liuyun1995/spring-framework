package org.springframework.beans.factory.parsing;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.bean.definition.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;

//组件定义
public interface ComponentDefinition extends BeanMetadataElement {

	//获取组件名称
	String getName();

	//获取描述符
	String getDescription();

	//获取Bean定义数组
	BeanDefinition[] getBeanDefinitions();

	//获取内部Bean定义数组
	BeanDefinition[] getInnerBeanDefinitions();

	//获取Bean引用数组
	BeanReference[] getBeanReferences();

}
