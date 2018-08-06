package org.springframework.beans.property.accessor;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.property.PropertyValue;
import org.springframework.beans.property.PropertyValues;
import org.springframework.core.convert.TypeDescriptor;

import java.util.Map;

//属性访问器接口
public interface PropertyAccessor {

	//嵌套属性分隔符
	String NESTED_PROPERTY_SEPARATOR = ".";
	char NESTED_PROPERTY_SEPARATOR_CHAR = '.';

	//属性前缀
	String PROPERTY_KEY_PREFIX = "[";
	char PROPERTY_KEY_PREFIX_CHAR = '[';

	//属性后缀
	String PROPERTY_KEY_SUFFIX = "]";
	char PROPERTY_KEY_SUFFIX_CHAR = ']';

	//是否可读属性
	boolean isReadableProperty(String propertyName);

	//是否可写属性
	boolean isWritableProperty(String propertyName);

	//获取属性类型
	Class<?> getPropertyType(String propertyName) throws org.springframework.beans.exception.BeansException;

	//获取属性类型分隔符
	TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws org.springframework.beans.exception.BeansException;

	//获取属性值
	Object getPropertyValue(String propertyName) throws org.springframework.beans.exception.BeansException;

	//设置属性值
	void setPropertyValue(String propertyName, Object value) throws org.springframework.beans.exception.BeansException;

	//设置属性值
	void setPropertyValue(PropertyValue pv) throws org.springframework.beans.exception.BeansException;

	//设置属性值
	void setPropertyValues(Map<?, ?> map) throws org.springframework.beans.exception.BeansException;

	//设置属性值
	void setPropertyValues(org.springframework.beans.property.PropertyValues pvs) throws org.springframework.beans.exception.BeansException;

	//设置属性值
	void setPropertyValues(org.springframework.beans.property.PropertyValues pvs, boolean ignoreUnknown) throws org.springframework.beans.exception.BeansException;

	//设置属性值
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid) throws BeansException;

}
