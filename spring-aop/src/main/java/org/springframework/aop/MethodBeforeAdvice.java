package org.springframework.aop;

import java.lang.reflect.Method;

//方法前通知
public interface MethodBeforeAdvice extends BeforeAdvice {

	//前置调用
	void before(Method method, Object[] args, Object target) throws Throwable;

}
