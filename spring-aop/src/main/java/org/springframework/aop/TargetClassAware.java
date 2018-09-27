package org.springframework.aop;

//目标类型装配
public interface TargetClassAware {

	//获取目标类型
	Class<?> getTargetClass();

}
