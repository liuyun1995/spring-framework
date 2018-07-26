package org.springframework.beans;

//属性值接口
public interface PropertyValues {

	//获取所有属性值
	PropertyValue[] getPropertyValues();

	//获取指定属性值
	PropertyValue getPropertyValue(String propertyName);

	//改变属性值
	PropertyValues changesSince(PropertyValues old);

	//是否包含该属性
	boolean contains(String propertyName);

	//是否为空
	boolean isEmpty();

}
