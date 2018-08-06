package org.springframework.beans.factory;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.bean.definition.BeanDefinition;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.exception.NoSuchBeanDefinitionException;

import java.util.Iterator;

//可配置可列举的Bean工厂
public interface ConfigurableListableBeanFactory
		extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {

	//忽略依赖类型
	void ignoreDependencyType(Class<?> type);

	//忽略依赖接口
	void ignoreDependencyInterface(Class<?> ifc);

	//注册可解析的依赖
	void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue);

	//是否是自动装配候选者
	boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor) throws NoSuchBeanDefinitionException;

	//获取Bean定义
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	//获取Bean名称迭代器
	Iterator<String> getBeanNamesIterator();

	//清空元数据缓存
	void clearMetadataCache();

	//冻结配置信息
	void freezeConfiguration();

	//配置信息是否冻结
	boolean isConfigurationFrozen();

	//提前实例化单例
	void preInstantiateSingletons() throws BeansException;

}
