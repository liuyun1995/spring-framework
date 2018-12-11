package org.springframework.aop;

import java.lang.reflect.Method;

//返回通知之后
public interface AfterReturningAdvice extends AfterAdvice {

	void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable;

}
