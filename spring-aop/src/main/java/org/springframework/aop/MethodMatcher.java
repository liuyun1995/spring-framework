package org.springframework.aop;

import java.lang.reflect.Method;

//方法匹配
public interface MethodMatcher {

	//是否运行时
	boolean isRuntime();

	//匹配方法
	boolean matches(Method method, Class<?> targetClass);

	//匹配方法
	boolean matches(Method method, Class<?> targetClass, Object... args);

	MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;

}
