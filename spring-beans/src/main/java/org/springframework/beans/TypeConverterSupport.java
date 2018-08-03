package org.springframework.beans;

import java.lang.reflect.Field;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConverterNotFoundException;

//类型转换器助手
public abstract class TypeConverterSupport extends PropertyEditorRegistrySupport implements TypeConverter {

	//类型转换器装饰器
	TypeConverterDelegate typeConverterDelegate;

	//转换方法
	@Override
	public <T> T convertIfNecessary(Object value, Class<T> requiredType) throws TypeMismatchException {
		return doConvert(value, requiredType, null, null);
	}

	//转换方法
	@Override
	public <T> T convertIfNecessary(Object value, Class<T> requiredType, MethodParameter methodParam)
			throws TypeMismatchException {
		return doConvert(value, requiredType, methodParam, null);
	}

	//转换方法
	@Override
	public <T> T convertIfNecessary(Object value, Class<T> requiredType, Field field) throws TypeMismatchException {
		return doConvert(value, requiredType, null, field);
	}

	//核心转换方法
	private <T> T doConvert(Object value, Class<T> requiredType, MethodParameter methodParam, Field field)
			throws TypeMismatchException {
		try {
			//根据字段是否为空，调用类型转换助手的不同方法进行转换
			if (field != null) {
				return this.typeConverterDelegate.convertIfNecessary(value, requiredType, field);
			} else {
				return this.typeConverterDelegate.convertIfNecessary(value, requiredType, methodParam);
			}
		} catch (ConverterNotFoundException ex) {
			throw new ConversionNotSupportedException(value, requiredType, ex);
		} catch (ConversionException ex) {
			throw new TypeMismatchException(value, requiredType, ex);
		} catch (IllegalStateException ex) {
			throw new ConversionNotSupportedException(value, requiredType, ex);
		} catch (IllegalArgumentException ex) {
			throw new TypeMismatchException(value, requiredType, ex);
		}
	}

}
