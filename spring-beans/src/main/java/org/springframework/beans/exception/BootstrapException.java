package org.springframework.beans.exception;

import org.springframework.beans.exception.FatalBeanException;

@SuppressWarnings("serial")
public class BootstrapException extends FatalBeanException {
	
	public BootstrapException(String msg) {
		super(msg);
	}
	
	public BootstrapException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
