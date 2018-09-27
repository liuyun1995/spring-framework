package org.aopalliance.intercept;

//调用接口
public interface Invocation extends Joinpoint {

	//获取参数数组
	Object[] getArguments();

}
