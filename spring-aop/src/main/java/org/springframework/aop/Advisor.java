package org.springframework.aop;

import org.aopalliance.aop.Advice;

//代理顾问
public interface Advisor {

	//获取通知
	Advice getAdvice();

	//是否前置实例
	boolean isPerInstance();

}
