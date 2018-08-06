package org.springframework.beans.bean.definition;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

//Bean定义持有器
public class BeanDefinitionHolder implements BeanMetadataElement {

	private final BeanDefinition beanDefinition;   //Bean定义
	private final String beanName;                 //Bean名称
	private final String[] aliases;                //别名数组

	//构造器1
	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
		this(beanDefinition, beanName, null);
	}

	//构造器2
	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, String[] aliases) {
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		Assert.notNull(beanName, "Bean name must not be null");
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		this.aliases = aliases;
	}

	//构造器3
	public BeanDefinitionHolder(BeanDefinitionHolder beanDefinitionHolder) {
		Assert.notNull(beanDefinitionHolder, "BeanDefinitionHolder must not be null");
		this.beanDefinition = beanDefinitionHolder.getBeanDefinition();
		this.beanName = beanDefinitionHolder.getBeanName();
		this.aliases = beanDefinitionHolder.getAliases();
	}

	//获取Bean定义
	public BeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}

	//获取Bean名称
	public String getBeanName() {
		return this.beanName;
	}

	//获取别名数组
	public String[] getAliases() {
		return this.aliases;
	}

	//获取xml资源
	@Override
	public Object getSource() {
		return this.beanDefinition.getSource();
	}

	//匹配名称
	public boolean matchesName(String candidateName) {
		return (candidateName != null && (candidateName.equals(this.beanName)
				|| candidateName.equals(BeanFactoryUtils.transformedBeanName(this.beanName))
				|| ObjectUtils.containsElement(this.aliases, candidateName)));
	}

	//获取短描述符
	public String getShortDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Bean definition with name '").append(this.beanName).append("'");
		if (this.aliases != null) {
			sb.append(" and aliases [").append(StringUtils.arrayToCommaDelimitedString(this.aliases)).append("]");
		}
		return sb.toString();
	}

	//获取长描述符
	public String getLongDescription() {
		StringBuilder sb = new StringBuilder(getShortDescription());
		sb.append(": ").append(this.beanDefinition);
		return sb.toString();
	}

	@Override
	public String toString() {
		return getLongDescription();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanDefinitionHolder)) {
			return false;
		}
		BeanDefinitionHolder otherHolder = (BeanDefinitionHolder) other;
		return this.beanDefinition.equals(otherHolder.beanDefinition) && this.beanName.equals(otherHolder.beanName)
				&& ObjectUtils.nullSafeEquals(this.aliases, otherHolder.aliases);
	}

	@Override
	public int hashCode() {
		int hashCode = this.beanDefinition.hashCode();
		hashCode = 29 * hashCode + this.beanName.hashCode();
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.aliases);
		return hashCode;
	}

}
