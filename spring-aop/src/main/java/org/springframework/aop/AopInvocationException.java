package org.springframework.aop;

import org.springframework.core.NestedRuntimeException;

//AOP调用异常
@SuppressWarnings("serial")
public class AopInvocationException extends NestedRuntimeException {

	public AopInvocationException(String msg) {
		super(msg);
	}

	public AopInvocationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
