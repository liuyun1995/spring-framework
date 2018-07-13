package org.springframework.beans.factory;

//Bean加载器装配
public interface BeanClassLoaderAware extends Aware {
	
	void setBeanClassLoader(ClassLoader classLoader);

}
