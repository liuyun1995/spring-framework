package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;

//Bean定义解析器
public interface BeanDefinitionParser {

	//解析方法
	BeanDefinition parse(Element element, ParserContext parserContext);

}
