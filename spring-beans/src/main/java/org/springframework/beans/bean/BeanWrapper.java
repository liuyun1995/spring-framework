package org.springframework.beans.bean;

import org.springframework.beans.exception.InvalidPropertyException;
import org.springframework.beans.property.accessor.ConfigurablePropertyAccessor;
import java.beans.PropertyDescriptor;

//Bean包装器接口
public interface BeanWrapper extends ConfigurablePropertyAccessor {

	//设置自增集合限制
	void setAutoGrowCollectionLimit(int autoGrowCollectionLimit);

	//获取自增集合限制
	int getAutoGrowCollectionLimit();

	//获取包装实例
	Object getWrappedInstance();

	//获取包装类型
	Class<?> getWrappedClass();

	//获取属性描述符集合
	PropertyDescriptor[] getPropertyDescriptors();

	//获取属性描述符
	PropertyDescriptor getPropertyDescriptor(String propertyName) throws InvalidPropertyException;

}
