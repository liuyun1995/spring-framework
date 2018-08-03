package org.springframework.beans.exception;

import org.springframework.beans.exception.FatalBeanException;

@SuppressWarnings("serial")
public class BeanExpressionException extends FatalBeanException {
	
	public BeanExpressionException(String msg) {
		super(msg);
	}
	
	public BeanExpressionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
