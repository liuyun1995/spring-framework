package org.springframework.aop.framework;

//AOP代理工厂
public interface AopProxyFactory {

	//创建AOP代理
	AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException;

}
