package org.springframework.beans.bean.definition;

import org.springframework.beans.exception.BeanDefinitionValidationException;
import org.springframework.beans.property.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.util.ObjectUtils;

//子类Bean定义
@SuppressWarnings("serial")
public class ChildBeanDefinition extends AbstractBeanDefinition {

	private String parentName;   //父类名称

	//构造器1
	public ChildBeanDefinition(String parentName) {
		super();
		this.parentName = parentName;
	}

	//构造器2
	public ChildBeanDefinition(String parentName, MutablePropertyValues pvs) {
		super(null, pvs);
		this.parentName = parentName;
	}

	//构造器3
	public ChildBeanDefinition(
			String parentName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		this.parentName = parentName;
	}

	//构造器4
	public ChildBeanDefinition(
			String parentName, Class<?> beanClass, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		this.parentName = parentName;
		setBeanClass(beanClass);
	}

	//构造器5
	public ChildBeanDefinition(
			String parentName, String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {

		super(cargs, pvs);
		this.parentName = parentName;
		setBeanClassName(beanClassName);
	}

	//构造器6
	public ChildBeanDefinition(ChildBeanDefinition original) {
		super(original);
	}

	//设置父类名称
	@Override
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	//获取父类名称
	@Override
	public String getParentName() {
		return this.parentName;
	}

	//验证方法
	@Override
	public void validate() throws BeanDefinitionValidationException {
		super.validate();
		if (this.parentName == null) {
			throw new BeanDefinitionValidationException("'parentName' must be set in ChildBeanDefinition");
		}
	}

	//克隆Bean定义
	@Override
	public AbstractBeanDefinition cloneBeanDefinition() {
		return new ChildBeanDefinition(this);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ChildBeanDefinition)) {
			return false;
		}
		ChildBeanDefinition that = (ChildBeanDefinition) other;
		return (ObjectUtils.nullSafeEquals(this.parentName, that.parentName) && super.equals(other));
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(this.parentName) * 29 + super.hashCode();
	}

	@Override
	public String toString() {
		return "Child bean with parent '" + this.parentName + "': " + super.toString();
	}

}
