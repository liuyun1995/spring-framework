package org.springframework.aop;

import java.io.Serializable;
import java.lang.reflect.Method;

//方法匹配器
@SuppressWarnings("serial")
class TrueMethodMatcher implements MethodMatcher, Serializable {

	public static final TrueMethodMatcher INSTANCE = new TrueMethodMatcher();

	private TrueMethodMatcher() {}

	@Override
	public boolean isRuntime() {
		return false;
	}

	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		return true;
	}

	@Override
	public boolean matches(Method method, Class<?> targetClass, Object... args) {
		// Should never be invoked as isRuntime returns false.
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "MethodMatcher.TRUE";
	}

	private Object readResolve() {
		return INSTANCE;
	}

}
