package org.springframework.core.convert;

import org.springframework.util.ObjectUtils;

@SuppressWarnings("serial")
public class ConversionFailedException extends ConversionException {

	private final TypeDescriptor sourceType;

	private final TypeDescriptor targetType;

	private final Object value;

	//构造器
	public ConversionFailedException(TypeDescriptor sourceType, TypeDescriptor targetType, Object value, Throwable cause) {
		super("Failed to convert from type [" + sourceType + "] to type [" + targetType +
				"] for value '" + ObjectUtils.nullSafeToString(value) + "'", cause);
		this.sourceType = sourceType;
		this.targetType = targetType;
		this.value = value;
	}

	//获取资源类型
	public TypeDescriptor getSourceType() {
		return this.sourceType;
	}

	//获取目标类型
	public TypeDescriptor getTargetType() {
		return this.targetType;
	}

	//获取值
	public Object getValue() {
		return this.value;
	}

}
