package org.springframework.beans.factory.config;

//单例Bean注册器
public interface SingletonBeanRegistry {
	
	void registerSingleton(String beanName, Object singletonObject);

	Object getSingleton(String beanName);
	
	boolean containsSingleton(String beanName);

	String[] getSingletonNames();

	int getSingletonCount();
	
	Object getSingletonMutex();

}
