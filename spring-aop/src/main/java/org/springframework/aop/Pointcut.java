package org.springframework.aop;

//切入点
public interface Pointcut {

	//获取类型过滤器
	ClassFilter getClassFilter();

	//获取方法匹配器
	MethodMatcher getMethodMatcher();

	Pointcut TRUE = TruePointcut.INSTANCE;

}
