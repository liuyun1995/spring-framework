package org.aopalliance.intercept;

import java.lang.reflect.Method;

//方法调用
public interface MethodInvocation extends Invocation {

	//获取方法
	Method getMethod();

}
