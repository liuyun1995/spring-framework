package org.springframework.beans.factory.xml;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.xml.parser.BeanDefinitionParser;
import org.springframework.beans.factory.xml.parser.ParserContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

//名称空间处理器助手
public abstract class NamespaceHandlerSupport implements NamespaceHandler {

    //Bean定义解析器
    private final Map<String, org.springframework.beans.factory.xml.parser.BeanDefinitionParser> parsers = new HashMap<String, org.springframework.beans.factory.xml.parser.BeanDefinitionParser>();
    //元素-Bean定义装饰器
    private final Map<String, BeanDefinitionDecorator> decorators = new HashMap<String, BeanDefinitionDecorator>();
    //属性-Bean定义装饰器
    private final Map<String, BeanDefinitionDecorator> attributeDecorators = new HashMap<String, BeanDefinitionDecorator>();

    //解析方法
    @Override
    public BeanDefinition parse(Element element, org.springframework.beans.factory.xml.parser.ParserContext parserContext) {
        return findParserForElement(element, parserContext).parse(element, parserContext);
    }

    //为元素寻找解析器
    private org.springframework.beans.factory.xml.parser.BeanDefinitionParser findParserForElement(Element element, org.springframework.beans.factory.xml.parser.ParserContext parserContext) {
        String localName = parserContext.getDelegate().getLocalName(element);
        org.springframework.beans.factory.xml.parser.BeanDefinitionParser parser = this.parsers.get(localName);
        if (parser == null) {
            parserContext.getReaderContext().fatal(
                    "Cannot locate BeanDefinitionParser for element [" + localName + "]", element);
        }
        return parser;
    }

    //装饰方法
    @Override
    public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, org.springframework.beans.factory.xml.parser.ParserContext parserContext) {
        return findDecoratorForNode(node, parserContext).decorate(node, definition, parserContext);
    }

    //为节点寻找装饰器
    private BeanDefinitionDecorator findDecoratorForNode(Node node, ParserContext parserContext) {
        BeanDefinitionDecorator decorator = null;
        String localName = parserContext.getDelegate().getLocalName(node);
        if (node instanceof Element) {
            decorator = this.decorators.get(localName);
        } else if (node instanceof Attr) {
            decorator = this.attributeDecorators.get(localName);
        } else {
            parserContext.getReaderContext().fatal(
                    "Cannot decorate based on Nodes of type [" + node.getClass().getName() + "]", node);
        }
        if (decorator == null) {
            parserContext.getReaderContext().fatal("Cannot locate BeanDefinitionDecorator for " +
                    (node instanceof Element ? "element" : "attribute") + " [" + localName + "]", node);
        }
        return decorator;
    }

    protected final void registerBeanDefinitionParser(String elementName, BeanDefinitionParser parser) {
        this.parsers.put(elementName, parser);
    }

    protected final void registerBeanDefinitionDecorator(String elementName, BeanDefinitionDecorator dec) {
        this.decorators.put(elementName, dec);
    }

    protected final void registerBeanDefinitionDecoratorForAttribute(String attrName, BeanDefinitionDecorator dec) {
        this.attributeDecorators.put(attrName, dec);
    }

}
