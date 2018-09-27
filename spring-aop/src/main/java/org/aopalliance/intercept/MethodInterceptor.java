package org.aopalliance.intercept;

//方法拦截器
public interface MethodInterceptor extends Interceptor {
	
	//调用方法
	Object invoke(MethodInvocation invocation) throws Throwable;

}
