package org.springframework.beans.factory.config.factorybean;

import java.io.Serializable;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.bean.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.util.Assert;

public class ObjectFactoryCreatingFactoryBean extends AbstractFactoryBean<ObjectFactory<Object>> {

	private String targetBeanName;  //目标Bean名称

	//设置目标Bean名称
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	//属性设置之后执行
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(this.targetBeanName, "Property 'targetBeanName' is required");
		super.afterPropertiesSet();
	}

	//获取对象类型
	@Override
	public Class<?> getObjectType() {
		return ObjectFactory.class;
	}

	//创建实例
	@Override
	protected ObjectFactory<Object> createInstance() {
		return new TargetBeanObjectFactory(getBeanFactory(), this.targetBeanName);
	}

	//目标Bean对象工厂
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
