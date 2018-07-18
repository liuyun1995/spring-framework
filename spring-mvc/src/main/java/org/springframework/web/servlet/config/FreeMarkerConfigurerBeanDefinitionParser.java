package org.springframework.web.servlet.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Parse the <mvc:freemarker-configurer> MVC namespace element and register
 * FreeMarkerConfigurer bean
 *
 * @author Rossen Stoyanchev
 * @since 4.1
 */
public class FreeMarkerConfigurerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	public static final String BEAN_NAME = "mvcFreeMarkerConfigurer";


	@Override
	protected String getBeanClassName(Element element) {
		return "org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer";
	}

	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
		return BEAN_NAME;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		List<Element> childElements = DomUtils.getChildElementsByTagName(element, "template-loader-path");
		if (!childElements.isEmpty()) {
			List<String> locations = new ArrayList<String>(childElements.size());
			for (Element childElement : childElements) {
				locations.add(childElement.getAttribute("location"));
			}
			if (locations.isEmpty()) {
				locations.add("/WEB-INF/");
			}
			builder.addPropertyValue("templateLoaderPaths", locations.toArray(new String[locations.size()]));
		}
	}

}
