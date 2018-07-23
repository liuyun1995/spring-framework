package org.springframework.beans.factory.support;

import org.springframework.util.Assert;

//管理的数组
@SuppressWarnings("serial")
public class ManagedArray extends ManagedList<Object> {
	
	volatile Class<?> resolvedElementType;
	
	public ManagedArray(String elementTypeName, int size) {
		super(size);
		Assert.notNull(elementTypeName, "elementTypeName must not be null");
		setElementTypeName(elementTypeName);
	}

}
