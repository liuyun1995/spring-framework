package org.springframework.aop;

//目标类型装配
public interface TargetClassAware {

	Class<?> getTargetClass();

}
