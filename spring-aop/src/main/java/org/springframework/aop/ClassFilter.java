package org.springframework.aop;

//类型过滤器
public interface ClassFilter {

	//匹配类型
	boolean matches(Class<?> clazz);


	/**
	 * Canonical instance of a ClassFilter that matches all classes.
	 */
	ClassFilter TRUE = TrueClassFilter.INSTANCE;

}
