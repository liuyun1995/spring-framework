package org.springframework.aop;

//目标源
public interface TargetSource extends TargetClassAware {

	//获取目标类型
	@Override
	Class<?> getTargetClass();

	//是否是静态
	boolean isStatic();

	//获取目标对象
	Object getTarget() throws Exception;

	//释放目标对象
	void releaseTarget(Object target) throws Exception;

}
