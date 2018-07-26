package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface ListableBeanFactory extends BeanFactory {

	//是否包含指定的Bean
	boolean containsBeanDefinition(String beanName);

	//获取Bean的总数
	int getBeanDefinitionCount();

	//获取Bean名称集合
	String[] getBeanDefinitionNames();

	//根据类型获取Bean名称
	String[] getBeanNamesForType(ResolvableType type);

	//根据类型获取Bean名称
	String[] getBeanNamesForType(Class<?> type);

	//根据类型获取Bean名称
	String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);


	<T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;


	<T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException;

	//根据注解获取Bean名称
	String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType);


	Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;

	//获取Bean中的注解
	<A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException;

}
