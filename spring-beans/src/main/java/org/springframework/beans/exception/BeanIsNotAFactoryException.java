package org.springframework.beans.exception;

import org.springframework.beans.bean.factorybean.FactoryBean;

@SuppressWarnings("serial")
public class BeanIsNotAFactoryException extends BeanNotOfRequiredTypeException {
	
	public BeanIsNotAFactoryException(String name, Class<?> actualType) {
		super(name, FactoryBean.class, actualType);
	}

}
