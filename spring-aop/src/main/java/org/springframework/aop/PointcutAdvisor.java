package org.springframework.aop;

//切入点通知
public interface PointcutAdvisor extends Advisor {

	//获取切入点
	Pointcut getPointcut();

}
