package org.springframework.beans.factory.serviceloader;

import java.util.ServiceLoader;

import org.springframework.beans.factory.support.autowire.BeanClassLoaderAware;

public class ServiceLoaderFactoryBean extends AbstractServiceLoaderBasedFactoryBean implements BeanClassLoaderAware {

	@Override
	protected Object getObjectToExpose(ServiceLoader<?> serviceLoader) {
		return serviceLoader;
	}

	@Override
	public Class<?> getObjectType() {
		return ServiceLoader.class;
	}

}
