package org.springframework.beans.bean.definition;

//通用Bean定义
@SuppressWarnings("serial")
public class GenericBeanDefinition extends AbstractBeanDefinition {

	private String parentName;   //父类名称

	//构造器1
	public GenericBeanDefinition() {
		super();
	}

	//构造器2
	public GenericBeanDefinition(BeanDefinition original) {
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

	//克隆Bean定义
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
