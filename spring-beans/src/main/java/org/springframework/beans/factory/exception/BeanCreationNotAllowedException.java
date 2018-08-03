package org.springframework.beans.factory.exception;

import org.springframework.beans.factory.exception.BeanCreationException;

@SuppressWarnings("serial")
public class BeanCreationNotAllowedException extends BeanCreationException {
	
	public BeanCreationNotAllowedException(String beanName, String msg) {
		super(beanName, msg);
	}

}
