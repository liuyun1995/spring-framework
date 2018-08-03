package org.springframework.beans.exception;

@SuppressWarnings("serial")
public class BeanIsAbstractException extends BeanCreationException {
	
	public BeanIsAbstractException(String beanName) {
		super(beanName, "Bean definition is abstract");
	}

}
