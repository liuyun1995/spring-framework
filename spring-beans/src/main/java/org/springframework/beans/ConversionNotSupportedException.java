package org.springframework.beans;

import java.beans.PropertyChangeEvent;

@SuppressWarnings("serial")
public class ConversionNotSupportedException extends TypeMismatchException {
	
	public ConversionNotSupportedException(PropertyChangeEvent propertyChangeEvent,
			Class<?> requiredType, Throwable cause) {
		super(propertyChangeEvent, requiredType, cause);
	}
	
	public ConversionNotSupportedException(Object value, Class<?> requiredType, Throwable cause) {
		super(value, requiredType, cause);
	}

}
