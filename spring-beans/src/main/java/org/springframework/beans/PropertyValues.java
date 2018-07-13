package org.springframework.beans;


public interface PropertyValues {
	
	PropertyValue[] getPropertyValues();
	
	PropertyValue getPropertyValue(String propertyName);
	
	PropertyValues changesSince(PropertyValues old);
	
	boolean contains(String propertyName);
	
	boolean isEmpty();

}
