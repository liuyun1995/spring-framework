package org.springframework.beans.property;

import org.springframework.beans.TypeConverterSupport;
import org.springframework.beans.exception.BeansException;
import org.springframework.beans.exception.NotWritablePropertyException;
import org.springframework.beans.exception.NullValueInNestedPathException;
import org.springframework.beans.exception.PropertyAccessException;
import org.springframework.beans.exception.PropertyBatchUpdateException;
import org.springframework.beans.property.ConfigurablePropertyAccessor;
import org.springframework.beans.property.MutablePropertyValues;
import org.springframework.beans.property.PropertyValue;
import org.springframework.beans.property.PropertyValues;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//抽象属性获取器
public abstract class AbstractPropertyAccessor extends TypeConverterSupport implements ConfigurablePropertyAccessor {

	private boolean extractOldValueForEditor = false;
	private boolean autoGrowNestedPaths = false;

	@Override
	public void setExtractOldValueForEditor(boolean extractOldValueForEditor) {
		this.extractOldValueForEditor = extractOldValueForEditor;
	}

	@Override
	public boolean isExtractOldValueForEditor() {
		return this.extractOldValueForEditor;
	}

	@Override
	public void setAutoGrowNestedPaths(boolean autoGrowNestedPaths) {
		this.autoGrowNestedPaths = autoGrowNestedPaths;
	}

	@Override
	public boolean isAutoGrowNestedPaths() {
		return this.autoGrowNestedPaths;
	}

	//设置属性值
	@Override
	public void setPropertyValue(org.springframework.beans.property.PropertyValue pv) throws org.springframework.beans.exception.BeansException {
		setPropertyValue(pv.getName(), pv.getValue());
	}

	//设置属性值
	@Override
	public void setPropertyValues(Map<?, ?> map) throws org.springframework.beans.exception.BeansException {
		setPropertyValues(new org.springframework.beans.property.MutablePropertyValues(map));
	}

	//设置属性值
	@Override
	public void setPropertyValues(org.springframework.beans.property.PropertyValues pvs) throws org.springframework.beans.exception.BeansException {
		setPropertyValues(pvs, false, false);
	}

	//设置属性值
	@Override
	public void setPropertyValues(org.springframework.beans.property.PropertyValues pvs, boolean ignoreUnknown) throws org.springframework.beans.exception.BeansException {
		setPropertyValues(pvs, ignoreUnknown, false);
	}

	//设置属性值
	@Override
	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid)
			throws org.springframework.beans.exception.BeansException {

		List<org.springframework.beans.exception.PropertyAccessException> propertyAccessExceptions = null;
		List<org.springframework.beans.property.PropertyValue> propertyValues = (pvs instanceof org.springframework.beans.property.MutablePropertyValues
				? ((MutablePropertyValues) pvs).getPropertyValueList()
				: Arrays.asList(pvs.getPropertyValues()));
		for (PropertyValue pv : propertyValues) {
			try {
				// This method may throw any BeansException, which won't be caught
				// here, if there is a critical failure such as no matching field.
				// We can attempt to deal only with less serious exceptions.
				setPropertyValue(pv);
			} catch (NotWritablePropertyException ex) {
				if (!ignoreUnknown) {
					throw ex;
				}
				// Otherwise, just ignore it and continue...
			} catch (NullValueInNestedPathException ex) {
				if (!ignoreInvalid) {
					throw ex;
				}
				// Otherwise, just ignore it and continue...
			} catch (org.springframework.beans.exception.PropertyAccessException ex) {
				if (propertyAccessExceptions == null) {
					propertyAccessExceptions = new LinkedList<org.springframework.beans.exception.PropertyAccessException>();
				}
				propertyAccessExceptions.add(ex);
			}
		}

		// If we encountered individual exceptions, throw the composite exception.
		if (propertyAccessExceptions != null) {
			org.springframework.beans.exception.PropertyAccessException[] paeArray = propertyAccessExceptions
					.toArray(new PropertyAccessException[propertyAccessExceptions.size()]);
			throw new PropertyBatchUpdateException(paeArray);
		}
	}

	//获取属性类型
	@Override
	public Class<?> getPropertyType(String propertyPath) {
		return null;
	}

	//获取属性值
	@Override
	public abstract Object getPropertyValue(String propertyName) throws org.springframework.beans.exception.BeansException;

	//设置属性值
	@Override
	public abstract void setPropertyValue(String propertyName, Object value) throws BeansException;

}
