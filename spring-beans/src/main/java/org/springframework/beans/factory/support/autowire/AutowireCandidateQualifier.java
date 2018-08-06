package org.springframework.beans.factory.support.autowire;

import org.springframework.beans.bean.BeanMetadataAttributeAccessor;
import org.springframework.util.Assert;

//自动装配候选者合格者
@SuppressWarnings("serial")
public class AutowireCandidateQualifier extends BeanMetadataAttributeAccessor {

	public static String VALUE_KEY = "value";
	private final String typeName;
	
	public AutowireCandidateQualifier(Class<?> type) {
		this(type.getName());
	}
	
	public AutowireCandidateQualifier(String typeName) {
		Assert.notNull(typeName, "Type name must not be null");
		this.typeName = typeName;
	}

	public AutowireCandidateQualifier(Class<?> type, Object value) {
		this(type.getName(), value);
	}

	public AutowireCandidateQualifier(String typeName, Object value) {
		Assert.notNull(typeName, "Type name must not be null");
		this.typeName = typeName;
		setAttribute(VALUE_KEY, value);
	}

	//获取类型名称
	public String getTypeName() {
		return this.typeName;
	}

}
