package org.springframework.beans.factory.parsing;

import org.springframework.beans.bean.BeanMetadataElement;
import org.springframework.util.Assert;

//别名定义
public class AliasDefinition implements BeanMetadataElement {

	private final String beanName;   //Bean名称
	private final String alias;      //别名定义
	private final Object source;     //资源
	
	public AliasDefinition(String beanName, String alias) {
		this(beanName, alias, null);
	}
	
	public AliasDefinition(String beanName, String alias, Object source) {
		Assert.notNull(beanName, "Bean name must not be null");
		Assert.notNull(alias, "Alias must not be null");
		this.beanName = beanName;
		this.alias = alias;
		this.source = source;
	}
	
	public final String getBeanName() {
		return this.beanName;
	}
	
	public final String getAlias() {
		return this.alias;
	}

	@Override
	public final Object getSource() {
		return this.source;
	}

}
