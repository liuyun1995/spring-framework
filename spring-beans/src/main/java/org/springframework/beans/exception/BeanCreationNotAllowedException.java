package org.springframework.beans.exception;

@SuppressWarnings("serial")
public class BeanCreationNotAllowedException extends BeanCreationException {
	
	public BeanCreationNotAllowedException(String beanName, String msg) {
		super(beanName, msg);
	}

}
