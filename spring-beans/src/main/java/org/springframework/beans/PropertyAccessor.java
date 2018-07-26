package org.springframework.beans;

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
	Class<?> getPropertyType(String propertyName) throws BeansException;

	//获取属性类型分隔符
	TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException;

	//获取属性值
	Object getPropertyValue(String propertyName) throws BeansException;

	//设置属性值
	void setPropertyValue(String propertyName, Object value) throws BeansException;

	//设置属性值
	void setPropertyValue(PropertyValue pv) throws BeansException;

	//设置属性值
	void setPropertyValues(Map<?, ?> map) throws BeansException;

	//设置属性值
	void setPropertyValues(PropertyValues pvs) throws BeansException;

	//设置属性值
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown) throws BeansException;

	//设置属性值
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid) throws BeansException;

}
