package org.springframework.beans.factory.config;

import org.springframework.beans.factory.NamedBean;
import org.springframework.util.Assert;

//命名Bean持有器
public class NamedBeanHolder<T> implements NamedBean {

	private final String beanName;   //Bean名称
	private final T beanInstance;    //Bean实例

	public NamedBeanHolder(String beanName, T beanInstance) {
		Assert.notNull(beanName, "Bean name must not be null");
		this.beanName = beanName;
		this.beanInstance = beanInstance;
	}

	@Override
	public String getBeanName() {
		return this.beanName;
	}

	public T getBeanInstance() {
		return this.beanInstance;
	}

}
