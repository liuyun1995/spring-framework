package org.springframework.aop.framework;

//AOP代理接口
public interface AopProxy {

	//获取代理对象
	Object getProxy();

	//获取代理对象
	Object getProxy(ClassLoader classLoader);

}
