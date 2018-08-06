package org.springframework.beans.support;

import java.lang.reflect.Method;

//方法替代者
public interface MethodReplacer {

	//替代方法
	Object reimplement(Object obj, Method method, Object[] args) throws Throwable;

}
