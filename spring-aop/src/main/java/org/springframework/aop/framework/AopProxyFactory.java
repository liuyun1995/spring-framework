package org.springframework.aop.framework;

//AOP代理工厂
public interface AopProxyFactory {

	AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException;

}
