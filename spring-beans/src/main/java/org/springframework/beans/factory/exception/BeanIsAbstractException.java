package org.springframework.beans.factory.exception;

import org.springframework.beans.factory.exception.BeanCreationException;

@SuppressWarnings("serial")
public class BeanIsAbstractException extends BeanCreationException {
	
	public BeanIsAbstractException(String beanName) {
		super(beanName, "Bean definition is abstract");
	}

}
