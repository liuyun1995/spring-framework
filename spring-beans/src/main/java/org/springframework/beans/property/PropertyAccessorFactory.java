package org.springframework.beans;

import org.springframework.beans.property.ConfigurablePropertyAccessor;

//属性访问工厂
public abstract class PropertyAccessorFactory {

	//Bean属性访问
	public static BeanWrapper forBeanPropertyAccess(Object target) {
		return new BeanWrapperImpl(target);
	}

	//直接字段访问
	public static ConfigurablePropertyAccessor forDirectFieldAccess(Object target) {
		return new DirectFieldAccessor(target);
	}

}
