package org.springframework.aop;

//切入点通知
public interface PointcutAdvisor extends Advisor {

	Pointcut getPointcut();

}
