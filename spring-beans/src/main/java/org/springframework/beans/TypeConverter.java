package org.springframework.beans;

import org.springframework.beans.exception.TypeMismatchException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Field;

//类型转换器
public interface TypeConverter {

	//类型转换方法
	<T> T convertIfNecessary(Object value, Class<T> requiredType) throws org.springframework.beans.exception.TypeMismatchException;

	//类型转换方法
	<T> T convertIfNecessary(Object value, Class<T> requiredType, MethodParameter methodParam) throws org.springframework.beans.exception.TypeMismatchException;

	//类型转换方法
	<T> T convertIfNecessary(Object value, Class<T> requiredType, Field field) throws TypeMismatchException;

}
