package org.springframework.beans.factory.bean.factory;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.NamedBeanHolder;

import java.util.Set;

//可自动装配的Bean工厂
public interface AutowireCapableBeanFactory extends BeanFactory {

	int AUTOWIRE_NO = 0;                    //不自动装配
	int AUTOWIRE_BY_NAME = 1;               //根据名称自动装配
	int AUTOWIRE_BY_TYPE = 2;               //根据类型自动装配
	int AUTOWIRE_CONSTRUCTOR = 3;           //根据构造器自动装配
	@Deprecated
	int AUTOWIRE_AUTODETECT = 4;            //通过自动检测进行装配

	//创建Bean对象(Bean类型)
	<T> T createBean(Class<T> beanClass) throws BeansException;

	//自动装配Bean
	void autowireBean(Object existingBean) throws BeansException;

	//配置Bean
	Object configureBean(Object existingBean, String beanName) throws BeansException;

	//创建Bean对象
	Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

	//自动装配Bean
	Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

	//装配Bean属性
	void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck) throws BeansException;

	//应用Bean属性值
	void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException;

	//初始化Bean
	Object initializeBean(Object existingBean, String beanName) throws BeansException;

	//初始化前加工方法
	Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws BeansException;

	//初始化后加工方法
	Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeansException;

	//销毁Bean实例
	void destroyBean(Object existingBean);

	//解析命名Bean
	<T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException;

	//解析依赖
	Object resolveDependency(DependencyDescriptor descriptor, String requestingBeanName) throws BeansException;

	//解析依赖
	Object resolveDependency(DependencyDescriptor descriptor, String requestingBeanName,
							 Set<String> autowiredBeanNames, TypeConverter typeConverter) throws BeansException;

}
