package org.springframework.aop;

import org.aopalliance.intercept.MethodInterceptor;

//介绍拦截器
public interface IntroductionInterceptor extends MethodInterceptor, DynamicIntroductionAdvice {

}
