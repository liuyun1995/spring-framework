package org.springframework.beans.factory.xml.parser;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.springframework.beans.bean.definition.BeanDefinitionBuilder;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

//抽象简单Bean定义解析器
public abstract class AbstractSimpleBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	//解析方法
	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		NamedNodeMap attributes = element.getAttributes();
		for (int x = 0; x < attributes.getLength(); x++) {
			Attr attribute = (Attr) attributes.item(x);
			if (isEligibleAttribute(attribute, parserContext)) {
				String propertyName = extractPropertyName(attribute.getLocalName());
				Assert.state(StringUtils.hasText(propertyName),
						"Illegal property name returned from 'extractPropertyName(String)': cannot be null or empty.");
				builder.addPropertyValue(propertyName, attribute.getValue());
			}
		}
		postProcess(builder, element);
	}

	protected boolean isEligibleAttribute(Attr attribute, ParserContext parserContext) {
		String fullName = attribute.getName();
		return (!fullName.equals("xmlns") && !fullName.startsWith("xmlns:") &&
				isEligibleAttribute(parserContext.getDelegate().getLocalName(attribute)));
	}

	protected boolean isEligibleAttribute(String attributeName) {
		return !ID_ATTRIBUTE.equals(attributeName);
	}

	protected String extractPropertyName(String attributeName) {
		return Conventions.attributeNameToPropertyName(attributeName);
	}

	protected void postProcess(BeanDefinitionBuilder beanDefinition, Element element) {}

}
