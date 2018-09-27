package org.aopalliance.aop;

//切面异常
@SuppressWarnings("serial")
public class AspectException extends RuntimeException {

	public AspectException(String message) {
		super(message);
	}

	public AspectException(String message, Throwable cause) {
		super(message, cause);
	}

}
