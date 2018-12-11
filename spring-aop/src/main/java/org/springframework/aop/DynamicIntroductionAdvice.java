package org.springframework.aop;

import org.aopalliance.aop.Advice;

//动态介绍通知
public interface DynamicIntroductionAdvice extends Advice {

	/**
	 * Does this introduction advice implement the given interface?
	 * @param intf the interface to check
	 * @return whether the advice implements the specified interface
	 */
	boolean implementsInterface(Class<?> intf);

}
