package org.springframework.beans.exception;

import org.springframework.beans.exception.FatalBeanException;

@SuppressWarnings("serial")
public class BeanInitializationException extends FatalBeanException {
	
	public BeanInitializationException(String msg) {
		super(msg);
	}
	
	public BeanInitializationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
