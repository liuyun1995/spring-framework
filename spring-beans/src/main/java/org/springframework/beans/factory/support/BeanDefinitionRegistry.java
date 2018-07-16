package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.AliasRegistry;

//Bean定义注册器
public interface BeanDefinitionRegistry extends AliasRegistry {

	//注册Bean定义
	void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException;

	//删除Bean定义
	void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	//获取Bean定义
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	//是否包含该Bean
	boolean containsBeanDefinition(String beanName);

	//获取所有Bean名称
	String[] getBeanDefinitionNames();

	//获取Bean的总数
	int getBeanDefinitionCount();
	
	//Bean名称是否已使用
	boolean isBeanNameInUse(String beanName);

}