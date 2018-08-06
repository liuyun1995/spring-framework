package org.springframework.beans.bean;

import org.springframework.core.AttributeAccessorSupport;

//Bean元数据属性获取器
public class BeanMetadataAttributeAccessor extends AttributeAccessorSupport implements BeanMetadataElement {

	private Object source;

	//设置资源
	public void setSource(Object source) {
		this.source = source;
	}

	//获取资源
	@Override
	public Object getSource() {
		return this.source;
	}

	//添加元数据属性
	public void addMetadataAttribute(BeanMetadataAttribute attribute) {
		super.setAttribute(attribute.getName(), attribute);
	}

	//获取元数据属性
	public BeanMetadataAttribute getMetadataAttribute(String name) {
		return (BeanMetadataAttribute) super.getAttribute(name);
	}

	//设置属性
	@Override
	public void setAttribute(String name, Object value) {
		super.setAttribute(name, new BeanMetadataAttribute(name, value));
	}

	//获取属性
	@Override
	public Object getAttribute(String name) {
		BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.getAttribute(name);
		return (attribute != null ? attribute.getValue() : null);
	}

	//移除属性
	@Override
	public Object removeAttribute(String name) {
		BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.removeAttribute(name);
		return (attribute != null ? attribute.getValue() : null);
	}

}
