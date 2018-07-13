package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

//Bean载入器
public interface BeanDefinitionReader {

	//获取Bean定义注册工厂
	BeanDefinitionRegistry getRegistry();
	
	//获取资源加载器
	ResourceLoader getResourceLoader();

	//获取Bean定义加载器
	ClassLoader getBeanClassLoader();

	//获取Bean名称生成器
	BeanNameGenerator getBeanNameGenerator();
	
	int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException;

	int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException;

	int loadBeanDefinitions(String location) throws BeanDefinitionStoreException;
	
	int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException;

}
