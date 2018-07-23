package org.springframework.beans.factory.config;

import java.io.Serializable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.util.Assert;

public class ObjectFactoryCreatingFactoryBean extends AbstractFactoryBean<ObjectFactory<Object>> {

	private String targetBeanName;

	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(this.targetBeanName, "Property 'targetBeanName' is required");
		super.afterPropertiesSet();
	}

	@Override
	public Class<?> getObjectType() {
		return ObjectFactory.class;
	}

	@Override
	protected ObjectFactory<Object> createInstance() {
		return new TargetBeanObjectFactory(getBeanFactory(), this.targetBeanName);
	}

	@SuppressWarnings("serial")
	private static class TargetBeanObjectFactory implements ObjectFactory<Object>, Serializable {

		private final BeanFactory beanFactory;

		private final String targetBeanName;

		public TargetBeanObjectFactory(BeanFactory beanFactory, String targetBeanName) {
			this.beanFactory = beanFactory;
			this.targetBeanName = targetBeanName;
		}

		@Override
		public Object getObject() throws BeansException {
			return this.beanFactory.getBean(this.targetBeanName);
		}
	}

}
