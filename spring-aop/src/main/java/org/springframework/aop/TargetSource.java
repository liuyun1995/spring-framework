package org.springframework.aop;

//目标源
public interface TargetSource extends TargetClassAware {

	@Override
	Class<?> getTargetClass();

	boolean isStatic();

	Object getTarget() throws Exception;

	void releaseTarget(Object target) throws Exception;

}
