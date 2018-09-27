package org.springframework.aop.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;

//切面组件定义
public class AspectComponentDefinition extends CompositeComponentDefinition {

	private final BeanDefinition[] beanDefinitions;  //Bean定义集合
	private final BeanReference[] beanReferences;    //Bean引用集合

	public AspectComponentDefinition(
			String aspectName, BeanDefinition[] beanDefinitions, BeanReference[] beanReferences, Object source) {
		super(aspectName, source);
		this.beanDefinitions = (beanDefinitions != null ? beanDefinitions : new BeanDefinition[0]);
		this.beanReferences = (beanReferences != null ? beanReferences : new BeanReference[0]);
	}

	@Override
	public BeanDefinition[] getBeanDefinitions() {
		return this.beanDefinitions;
	}

	@Override
	public BeanReference[] getBeanReferences() {
		return this.beanReferences;
	}

}
