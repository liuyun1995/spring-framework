package org.springframework.beans.property;

import org.springframework.beans.bean.BeanMetadataAttributeAccessor;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;

//属性值
@SuppressWarnings("serial")
public class PropertyValue extends BeanMetadataAttributeAccessor implements Serializable {

	private final String name;                 //属性名
	private final Object value;                //属性值
	private boolean optional = false;          //是否原始属性
	private boolean converted = false;         //是否已经转换
	private Object convertedValue;             //转换后的值
	volatile Boolean conversionNecessary;      //是否版本必须
	transient volatile Object resolvedTokens;  //解析标记

	//构造器1
	public PropertyValue(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	//构造器2
	public PropertyValue(PropertyValue original) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = original.getValue();
		this.optional = original.isOptional();
		this.converted = original.converted;
		this.convertedValue = original.convertedValue;
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		setSource(original.getSource());
		copyAttributesFrom(original);
	}

	//构造器3
	public PropertyValue(PropertyValue original, Object newValue) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = newValue;
		this.optional = original.isOptional();
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		setSource(original);
		copyAttributesFrom(original);
	}

	//获取属性名
	public String getName() {
		return this.name;
	}

	//获取属性值
	public Object getValue() {
		return this.value;
	}

	//获取原始属性值
	public PropertyValue getOriginalPropertyValue() {
		PropertyValue original = this;
		Object source = getSource();
		while (source instanceof PropertyValue && source != original) {
			original = (PropertyValue) source;
			source = original.getSource();
		}
		return original;
	}

	//设置是否原始属性
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	//获取是否原始属性
	public boolean isOptional() {
		return this.optional;
	}

	//是否已经转换过
	public synchronized boolean isConverted() {
		return this.converted;
	}

	//设置转换后的值
	public synchronized void setConvertedValue(Object value) {
		this.converted = true;
		this.convertedValue = value;
	}

	//获取转换后的值
	public synchronized Object getConvertedValue() {
		return this.convertedValue;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PropertyValue)) {
			return false;
		}
		PropertyValue otherPv = (PropertyValue) other;
		return (this.name.equals(otherPv.name) &&
				ObjectUtils.nullSafeEquals(this.value, otherPv.value) &&
				ObjectUtils.nullSafeEquals(getSource(), otherPv.getSource()));
	}

	@Override
	public int hashCode() {
		return this.name.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.value);
	}

	@Override
	public String toString() {
		return "bean property '" + this.name + "'";
	}

}
