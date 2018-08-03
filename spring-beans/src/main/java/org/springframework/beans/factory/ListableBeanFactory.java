package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.exception.NoSuchBeanDefinitionException;
import org.springframework.core.ResolvableType;

import java.lang.annotation.Annotation;
import java.util.Map;

//可列举的Bean工厂
public interface ListableBeanFactory extends BeanFactory {

	//是否包含指定Bean
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

	//根据类型获取Bean
	<T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;

	//根据类型获取Bean
	<T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException;

	//根据注解获取Bean名称
	String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType);

	//获取包含指定注解的Bean
	Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;

	//获取Bean中特定的注解
	<A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException;

}
