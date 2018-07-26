package org.springframework.beans.factory.parsing;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;

//抽象组件定义
public abstract class AbstractComponentDefinition implements ComponentDefinition {

	//获取描述符
	@Override
	public String getDescription() {
		return getName();
	}

	//获取Bean定义数组
	@Override
	public BeanDefinition[] getBeanDefinitions() {
		//返回空数组
		return new BeanDefinition[0];
	}

	//获取内部Bean定义数组
	@Override
	public BeanDefinition[] getInnerBeanDefinitions() {
		return new BeanDefinition[0];
	}

	//获取Bean引用数组
	@Override
	public BeanReference[] getBeanReferences() {
		return new BeanReference[0];
	}

	@Override
	public String toString() {
		return getDescription();
	}

}
