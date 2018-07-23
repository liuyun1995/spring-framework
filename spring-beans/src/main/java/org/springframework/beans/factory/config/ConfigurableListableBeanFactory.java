package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.Iterator;

public interface ConfigurableListableBeanFactory
		extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {

	void ignoreDependencyType(Class<?> type);

	void ignoreDependencyInterface(Class<?> ifc);

	void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue);

	boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor) throws NoSuchBeanDefinitionException;

	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	Iterator<String> getBeanNamesIterator();

	void clearMetadataCache();

	void freezeConfiguration();

	boolean isConfigurationFrozen();

	void preInstantiateSingletons() throws BeansException;

}
