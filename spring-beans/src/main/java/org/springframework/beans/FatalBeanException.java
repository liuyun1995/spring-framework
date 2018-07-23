package org.springframework.beans;

//致命的Bean异常
@SuppressWarnings("serial")
public class FatalBeanException extends BeansException {
	
	public FatalBeanException(String msg) {
		super(msg);
	}
	
	public FatalBeanException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
