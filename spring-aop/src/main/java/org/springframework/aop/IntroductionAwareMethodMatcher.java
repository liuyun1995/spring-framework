package org.springframework.aop;

import java.lang.reflect.Method;

//方法匹配器
public interface IntroductionAwareMethodMatcher extends MethodMatcher {

	//匹配方法
	boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions);

}
