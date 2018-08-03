package org.springframework.beans.factory.support;

import org.springframework.beans.factory.exception.BeanDefinitionStoreException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

//Bean定义阅读器
public interface BeanDefinitionReader {

	//获取Bean定义注册工厂
	BeanDefinitionRegistry getRegistry();
	
	//获取资源加载器
	ResourceLoader getResourceLoader();

	//获取Bean定义加载器
	ClassLoader getBeanClassLoader();

	//获取Bean名称生成器
	BeanNameGenerator getBeanNameGenerator();

	//加载Bean定义
	int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException;

	//加载Bean定义
	int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException;

	//加载Bean定义
	int loadBeanDefinitions(String location) throws BeanDefinitionStoreException;

	//加载Bean定义
	int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException;

}
