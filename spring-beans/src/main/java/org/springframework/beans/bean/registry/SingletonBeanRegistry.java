package org.springframework.beans.bean.registry;

//单例Bean注册器
public interface SingletonBeanRegistry {

	//注册单例Bean
	void registerSingleton(String beanName, Object singletonObject);

	//根据名称获取单例
	Object getSingleton(String beanName);

	//是否包含指定单例
	boolean containsSingleton(String beanName);

	//获取所有单例名称
	String[] getSingletonNames();

	//获取单例Bean总数
	int getSingletonCount();

	//获取单例互斥体
	Object getSingletonMutex();

}
