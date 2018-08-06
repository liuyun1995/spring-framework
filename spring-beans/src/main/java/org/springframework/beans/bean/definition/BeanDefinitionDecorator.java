package org.springframework.beans.bean.definition;

import org.springframework.beans.factory.xml.parser.ParserContext;
import org.w3c.dom.Node;

//Bean定义装饰器
public interface BeanDefinitionDecorator {

	//装饰方法
	BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext);

}
