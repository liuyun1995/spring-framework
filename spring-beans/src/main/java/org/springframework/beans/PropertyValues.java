package org.springframework.beans;

//属性值接口
public interface PropertyValues {
	
	PropertyValue[] getPropertyValues();
	
	PropertyValue getPropertyValue(String propertyName);
	
	PropertyValues changesSince(PropertyValues old);
	
	boolean contains(String propertyName);
	
	boolean isEmpty();

}
