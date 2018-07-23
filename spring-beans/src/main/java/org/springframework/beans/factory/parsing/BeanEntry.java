package org.springframework.beans.factory.parsing;

//Bean实体
public class BeanEntry implements ParseState.Entry {

	private String beanDefinitionName;

	public BeanEntry(String beanDefinitionName) {
		this.beanDefinitionName = beanDefinitionName;
	}

	@Override
	public String toString() {
		return "Bean '" + this.beanDefinitionName + "'";
	}

}
