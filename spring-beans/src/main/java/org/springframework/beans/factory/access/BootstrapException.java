package org.springframework.beans.factory.access;

import org.springframework.beans.FatalBeanException;

@SuppressWarnings("serial")
public class BootstrapException extends FatalBeanException {
	
	public BootstrapException(String msg) {
		super(msg);
	}
	
	public BootstrapException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
