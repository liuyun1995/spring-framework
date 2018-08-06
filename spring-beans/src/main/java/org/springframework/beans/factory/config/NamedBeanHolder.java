package org.springframework.beans.factory.config;

import org.springframework.beans.bean.NamedBean;
import org.springframework.util.Assert;

//命名Bean持有器
public class NamedBeanHolder<T> implements NamedBean {

	private final String beanName;   //Bean名称
	private final T beanInstance;    //Bean实例

	//构造器
	public NamedBeanHolder(String beanName, T beanInstance) {
		Assert.notNull(beanName, "Bean name must not be null");
		this.beanName = beanName;
		this.beanInstance = beanInstance;
	}

	//获取Bean名称
	@Override
	public String getBeanName() {
		return this.beanName;
	}

	//获取Bean实例
	public T getBeanInstance() {
		return this.beanInstance;
	}

}
