package org.springframework.beans.factory.xml;

import org.springframework.beans.factory.xml.parser.ParserContext;
import org.w3c.dom.Node;

import org.springframework.beans.factory.bean.definition.BeanDefinitionHolder;

//Bean定义装饰器
public interface BeanDefinitionDecorator {

	//装饰方法
	BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext);

}
