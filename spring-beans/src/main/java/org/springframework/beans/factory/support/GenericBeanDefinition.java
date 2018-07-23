package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinition;

//范型Bean定义
@SuppressWarnings("serial")
public class GenericBeanDefinition extends AbstractBeanDefinition {

	private String parentName;

	public GenericBeanDefinition() {
		super();
	}

	public GenericBeanDefinition(BeanDefinition original) {
		super(original);
	}

	@Override
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	@Override
	public String getParentName() {
		return this.parentName;
	}

	@Override
	public AbstractBeanDefinition cloneBeanDefinition() {
		return new GenericBeanDefinition(this);
	}

	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof GenericBeanDefinition && super.equals(other)));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Generic bean");
		if (this.parentName != null) {
			sb.append(" with parent '").append(this.parentName).append("'");
		}
		sb.append(": ").append(super.toString());
		return sb.toString();
	}

}
