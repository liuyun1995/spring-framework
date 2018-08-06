package org.springframework.beans.factory.xml.parser;

import org.w3c.dom.Element;

import org.springframework.beans.bean.definition.BeanDefinition;

//Bean定义解析器
public interface BeanDefinitionParser {

	//根据对应标签元素进行解析
	BeanDefinition parse(Element element, ParserContext parserContext);

}
