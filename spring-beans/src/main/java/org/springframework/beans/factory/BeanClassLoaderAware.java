package org.springframework.beans.factory;

//Bean加载器装配
public interface BeanClassLoaderAware extends Aware {

	//设置Bean加载器
	void setBeanClassLoader(ClassLoader classLoader);

}
