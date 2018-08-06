package org.springframework.beans;

import org.springframework.beans.exception.InvalidPropertyException;
import org.springframework.beans.exception.NotWritablePropertyException;
import org.springframework.beans.property.accessor.AbstractNestablePropertyAccessor;
import org.springframework.beans.property.PropertyMatches;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

//直接字段获取器
public class DirectFieldAccessor extends AbstractNestablePropertyAccessor {

	private final Map<String, FieldPropertyHandler> fieldMap = new HashMap<String, FieldPropertyHandler>();
	
	public DirectFieldAccessor(Object object) {
		super(object);
	}
	
	protected DirectFieldAccessor(Object object, String nestedPath, DirectFieldAccessor parent) {
		super(object, nestedPath, parent);
	}

	@Override
	protected FieldPropertyHandler getLocalPropertyHandler(String propertyName) {
		FieldPropertyHandler propertyHandler = this.fieldMap.get(propertyName);
		if (propertyHandler == null) {
			Field field = ReflectionUtils.findField(getWrappedClass(), propertyName);
			if (field != null) {
				propertyHandler = new FieldPropertyHandler(field);
			}
			this.fieldMap.put(propertyName, propertyHandler);
		}
		return propertyHandler;
	}

	@Override
	protected DirectFieldAccessor newNestedPropertyAccessor(Object object, String nestedPath) {
		return new DirectFieldAccessor(object, nestedPath, this);
	}

	@Override
	protected org.springframework.beans.exception.NotWritablePropertyException createNotWritablePropertyException(String propertyName) {
		org.springframework.beans.property.PropertyMatches matches = PropertyMatches.forField(propertyName, getRootClass());
		throw new NotWritablePropertyException(getRootClass(), getNestedPath() + propertyName,
				matches.buildErrorMessage(), matches.getPossibleMatches());
	}

	//字段属性处理器
	private class FieldPropertyHandler extends PropertyHandler {

		private final Field field;

		public FieldPropertyHandler(Field field) {
			super(field.getType(), true, true);
			this.field = field;
		}

		@Override
		public TypeDescriptor toTypeDescriptor() {
			return new TypeDescriptor(this.field);
		}

		@Override
		public ResolvableType getResolvableType() {
			return ResolvableType.forField(this.field);
		}

		@Override
		public TypeDescriptor nested(int level) {
			return TypeDescriptor.nested(this.field, level);
		}

		@Override
		public Object getValue() throws Exception {
			try {
				ReflectionUtils.makeAccessible(this.field);
				return this.field.get(getWrappedInstance());
			} catch (IllegalAccessException ex) {
				throw new org.springframework.beans.exception.InvalidPropertyException(getWrappedClass(), this.field.getName(), "Field is not accessible",
						ex);
			}
		}

		@Override
		public void setValue(Object object, Object value) throws Exception {
			try {
				ReflectionUtils.makeAccessible(this.field);
				this.field.set(object, value);
			} catch (IllegalAccessException ex) {
				throw new InvalidPropertyException(getWrappedClass(), this.field.getName(), "Field is not accessible",
						ex);
			}
		}
	}

}
