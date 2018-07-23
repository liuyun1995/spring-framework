package org.springframework.beans.factory.parsing;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;

//抽象组件定义
public abstract class AbstractComponentDefinition implements ComponentDefinition {

	@Override
	public String getDescription() {
		return getName();
	}

	/**
	 * Returns an empty array.
	 */
	@Override
	public BeanDefinition[] getBeanDefinitions() {
		return new BeanDefinition[0];
	}

	/**
	 * Returns an empty array.
	 */
	@Override
	public BeanDefinition[] getInnerBeanDefinitions() {
		return new BeanDefinition[0];
	}

	/**
	 * Returns an empty array.
	 */
	@Override
	public BeanReference[] getBeanReferences() {
		return new BeanReference[0];
	}

	/**
	 * Delegates to {@link #getDescription}.
	 */
	@Override
	public String toString() {
		return getDescription();
	}

}
