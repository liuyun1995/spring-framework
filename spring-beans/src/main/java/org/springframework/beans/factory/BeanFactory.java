package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.exception.NoSuchBeanDefinitionException;
import org.springframework.core.ResolvableType;

//Bean工厂接口
public interface BeanFactory {

	//工厂Bean前缀
	String FACTORY_BEAN_PREFIX = "&";

	//获取Bean
	Object getBean(String name) throws BeansException;

	//获取Bean
	<T> T getBean(String name, Class<T> requiredType) throws BeansException;

	//获取Bean
	Object getBean(String name, Object... args) throws BeansException;

	//获取Bean
	<T> T getBean(Class<T> requiredType) throws BeansException;

	//获取Bean
	<T> T getBean(Class<T> requiredType, Object... args) throws BeansException;

	//是否包含指定Bean
	boolean containsBean(String name);

	//指定Bean是否是单例
	boolean isSingleton(String name) throws org.springframework.beans.factory.exception.NoSuchBeanDefinitionException;

	//指定Bean是否是原型
	boolean isPrototype(String name) throws org.springframework.beans.factory.exception.NoSuchBeanDefinitionException;

	//是否类型匹配
	boolean isTypeMatch(String name, ResolvableType typeToMatch) throws org.springframework.beans.factory.exception.NoSuchBeanDefinitionException;

	//是否类型匹配
	boolean isTypeMatch(String name, Class<?> typeToMatch) throws org.springframework.beans.factory.exception.NoSuchBeanDefinitionException;

	//获取指定Bean的类型
	Class<?> getType(String name) throws NoSuchBeanDefinitionException;

	//获取指定Bean的别名
	String[] getAliases(String name);

}
