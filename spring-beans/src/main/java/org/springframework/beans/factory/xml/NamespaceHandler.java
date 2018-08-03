package org.springframework.beans.factory.xml;

import org.springframework.beans.factory.xml.parser.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.beans.factory.bean.definition.BeanDefinition;
import org.springframework.beans.factory.bean.definition.BeanDefinitionHolder;

//名称空间处理器
public interface NamespaceHandler {

	//初始化
	void init();

	//解析方法
	BeanDefinition parse(Element element, org.springframework.beans.factory.xml.parser.ParserContext parserContext);

	//修饰方法
	BeanDefinitionHolder decorate(Node source, BeanDefinitionHolder definition, ParserContext parserContext);

}
