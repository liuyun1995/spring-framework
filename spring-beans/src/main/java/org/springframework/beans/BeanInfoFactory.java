package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;

//Bean信息工厂
public interface BeanInfoFactory {
	
	BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException;

}
