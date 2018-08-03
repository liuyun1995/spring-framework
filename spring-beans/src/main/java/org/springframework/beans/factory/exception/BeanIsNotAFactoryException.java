package org.springframework.beans.factory.exception;

import org.springframework.beans.factory.config.factorybean.FactoryBean;

@SuppressWarnings("serial")
public class BeanIsNotAFactoryException extends BeanNotOfRequiredTypeException {
	
	public BeanIsNotAFactoryException(String name, Class<?> actualType) {
		super(name, FactoryBean.class, actualType);
	}

}