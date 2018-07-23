package org.springframework.beans.factory.serviceloader;

import java.util.ServiceLoader;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;


public abstract class AbstractServiceLoaderBasedFactoryBean extends AbstractFactoryBean<Object>
		implements BeanClassLoaderAware {

	private Class<?> serviceType;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	public void setServiceType(Class<?> serviceType) {
		this.serviceType = serviceType;
	}

	public Class<?> getServiceType() {
		return this.serviceType;
	}

	@Override
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	@Override
	protected Object createInstance() {
		Assert.notNull(getServiceType(), "Property 'serviceType' is required");
		return getObjectToExpose(ServiceLoader.load(getServiceType(), this.beanClassLoader));
	}

	protected abstract Object getObjectToExpose(ServiceLoader<?> serviceLoader);

}
