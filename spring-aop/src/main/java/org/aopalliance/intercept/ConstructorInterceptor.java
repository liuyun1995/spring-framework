package org.aopalliance.intercept;

//有构造器的拦截器接口
public interface ConstructorInterceptor extends Interceptor  {

	//构造方法
	Object construct(ConstructorInvocation invocation) throws Throwable;

}
