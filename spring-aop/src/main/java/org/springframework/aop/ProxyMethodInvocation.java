package org.springframework.aop;

import org.aopalliance.intercept.MethodInvocation;

//代理方法调用
public interface ProxyMethodInvocation extends MethodInvocation {

	//获取代理对象
	Object getProxy();

	//调用克隆
	MethodInvocation invocableClone();

	//调用克隆
	MethodInvocation invocableClone(Object... arguments);

	//设置参数
	void setArguments(Object... arguments);

	//设置用户属性
	void setUserAttribute(String key, Object value);

	//获取用户属性
	Object getUserAttribute(String key);

}
