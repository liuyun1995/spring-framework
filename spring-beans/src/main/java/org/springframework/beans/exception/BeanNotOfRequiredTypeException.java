package org.springframework.beans.exception;

import org.springframework.beans.exception.BeansException;
import org.springframework.util.ClassUtils;

@SuppressWarnings("serial")
public class BeanNotOfRequiredTypeException extends BeansException {

	private String beanName;
	private Class<?> requiredType;
	private Class<?> actualType;

	public BeanNotOfRequiredTypeException(String beanName, Class<?> requiredType, Class<?> actualType) {
		super("Bean named '" + beanName + "' is expected to be of type '" + ClassUtils.getQualifiedName(requiredType) +
				"' but was actually of type '" + ClassUtils.getQualifiedName(actualType) + "'");
		this.beanName = beanName;
		this.requiredType = requiredType;
		this.actualType = actualType;
	}

	public String getBeanName() {
		return this.beanName;
	}

	public Class<?> getRequiredType() {
		return this.requiredType;
	}

	public Class<?> getActualType() {
		return this.actualType;
	}

}
