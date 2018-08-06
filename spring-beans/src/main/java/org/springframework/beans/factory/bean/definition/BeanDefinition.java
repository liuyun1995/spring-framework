package org.springframework.beans.factory.bean.definition;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.property.MutablePropertyValues;
import org.springframework.core.AttributeAccessor;

//Bean定义
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {

	String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;  //单例
	String SCOPE_PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;  //原型
	int ROLE_APPLICATION = 0;
	int ROLE_SUPPORT = 1;
	int ROLE_INFRASTRUCTURE = 2;

	//-------------------可修改属性-------------------

	//设置父Bean名称
	void setParentName(String parentName);

	//获取父Bean名称
	String getParentName();

	//设置Bean类型名称
	void setBeanClassName(String beanClassName);

	//获取Bean类型名称
	String getBeanClassName();

	//设置Bean范围
	void setScope(String scope);

	//获取Bean范围
	String getScope();

	//设置是否懒加载
	void setLazyInit(boolean lazyInit);

	//是否懒加载
	boolean isLazyInit();

	//设置依赖关系
	void setDependsOn(String... dependsOn);

	//获取依赖关系
	String[] getDependsOn();

	//设置自动装配候选模式
	void setAutowireCandidate(boolean autowireCandidate);

	//是否自动装配候选模式
	boolean isAutowireCandidate();

	//设置是否主要的
	void setPrimary(boolean primary);

	//是否主要的
	boolean isPrimary();

	//设置工厂Bean名称
	void setFactoryBeanName(String factoryBeanName);

	//获取工厂Bean名称
	String getFactoryBeanName();

	//设置工厂方法名称
	void setFactoryMethodName(String factoryMethodName);

	//获取工厂方法名称
	String getFactoryMethodName();

	//获取构造器参数值
	ConstructorArgumentValues getConstructorArgumentValues();

	//获取属性值
	MutablePropertyValues getPropertyValues();

	//-------------------只读属性-------------------

	//是否单例
	boolean isSingleton();

	//是否原型
	boolean isPrototype();

	//是否抽象
	boolean isAbstract();

	//获取角色
	int getRole();

	//获取描述符
	String getDescription();

	//获取资源描述符
	String getResourceDescription();

	//获取原始Bean定义
	BeanDefinition getOriginatingBeanDefinition();

}
