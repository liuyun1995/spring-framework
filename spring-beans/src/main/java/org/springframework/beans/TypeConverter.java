package org.springframework.beans;

import org.springframework.core.MethodParameter;

import java.lang.reflect.Field;

//类型转换器
public interface TypeConverter {


	<T> T convertIfNecessary(Object value, Class<T> requiredType) throws TypeMismatchException;


	<T> T convertIfNecessary(Object value, Class<T> requiredType, MethodParameter methodParam) throws TypeMismatchException;


	<T> T convertIfNecessary(Object value, Class<T> requiredType, Field field) throws TypeMismatchException;


}
