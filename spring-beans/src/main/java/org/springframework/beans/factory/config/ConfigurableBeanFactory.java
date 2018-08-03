package org.springframework.beans.factory.config;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.exception.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.exception.NoSuchBeanDefinitionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.StringValueResolver;

import java.beans.PropertyEditor;
import java.security.AccessControlContext;

//可配置的Bean工厂
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

	String SCOPE_SINGLETON = "singleton";     //单例范围
	String SCOPE_PROTOTYPE = "prototype";     //原型范围

	//设置父Bean工厂
	void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException;

	//设置类型加载器
	void setBeanClassLoader(ClassLoader beanClassLoader);

	//获取类型加载器
	ClassLoader getBeanClassLoader();

	//设置临时加载器
	void setTempClassLoader(ClassLoader tempClassLoader);

	//获取临时加载器
	ClassLoader getTempClassLoader();

	//设置是否缓存Bean元数据
	void setCacheBeanMetadata(boolean cacheBeanMetadata);

	//是否缓存Bean元数据
	boolean isCacheBeanMetadata();

	//设置Bean表达式解析器
	void setBeanExpressionResolver(BeanExpressionResolver resolver);

	//获取Bean表达式解析器
	BeanExpressionResolver getBeanExpressionResolver();

	//设置转换服务者
	void setConversionService(ConversionService conversionService);

	//获取转换服务者
	ConversionService getConversionService();

	//添加属性编辑器注册器
	void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar);

	//注册外部属性编辑器
	void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass);

	//复制注册的属性编辑器
	void copyRegisteredEditorsTo(PropertyEditorRegistry registry);

	//设置类型转换器
	void setTypeConverter(TypeConverter typeConverter);

	//获取类型转换器
	TypeConverter getTypeConverter();

	//添加嵌入值解析器
	void addEmbeddedValueResolver(StringValueResolver valueResolver);

	//是否存在嵌入值解析器
	boolean hasEmbeddedValueResolver();

	//解析嵌入值
	String resolveEmbeddedValue(String value);

	//添加Bean后置处理器
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

	//获取Bean后置处理器数量
	int getBeanPostProcessorCount();

	//注册范围
	void registerScope(String scopeName, Scope scope);

	//获取已注册的范围名
	String[] getRegisteredScopeNames();

	//根据名称获取范围
	Scope getRegisteredScope(String scopeName);

	//获取访问控制上下文
	AccessControlContext getAccessControlContext();

	//复制配置信息
	void copyConfigurationFrom(ConfigurableBeanFactory otherFactory);

	//注册别名
	void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException;

	//解析别名
	void resolveAliases(StringValueResolver valueResolver);

	//获取合并的Bean定义
	BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	//是否是工厂Bean
	boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException;

	//设置当前Bean正在被创建
	void setCurrentlyInCreation(String beanName, boolean inCreation);

	//指定Bean是否正在被创建
	boolean isCurrentlyInCreation(String beanName);

	//注册Bean的依赖关系
	void registerDependentBean(String beanName, String dependentBeanName);

	//获取所有依赖该Bean的集合
	String[] getDependentBeans(String beanName);

	//获取所有被该Bean依赖的集合
	String[] getDependenciesForBean(String beanName);

	//销毁Bean实例
	void destroyBean(String beanName, Object beanInstance);

	//销毁范围Bean
	void destroyScopedBean(String beanName);

	//销毁所有单例Bean
	void destroySingletons();

}
