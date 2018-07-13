package org.springframework.beans.factory.access.el;

import javax.el.ELContext;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;

//¼òµ¥Bean EL½âÎöÆ÷
public class SimpleSpringBeanELResolver extends SpringBeanELResolver {

	private final BeanFactory beanFactory;
	
	public SimpleSpringBeanELResolver(BeanFactory beanFactory) {
		Assert.notNull(beanFactory, "BeanFactory must not be null");
		this.beanFactory = beanFactory;
	}

	@Override
	protected BeanFactory getBeanFactory(ELContext elContext) {
		return this.beanFactory;
	}

}
