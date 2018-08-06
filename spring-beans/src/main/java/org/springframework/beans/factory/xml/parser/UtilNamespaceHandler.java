package org.springframework.beans.factory.xml.parser;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.w3c.dom.Element;
import org.springframework.beans.bean.factorybean.FieldRetrievingFactoryBean;
import org.springframework.beans.bean.factorybean.ListFactoryBean;
import org.springframework.beans.bean.factorybean.MapFactoryBean;
import org.springframework.beans.bean.factorybean.PropertiesFactoryBean;
import org.springframework.beans.bean.factorybean.PropertyPathFactoryBean;
import org.springframework.beans.bean.factorybean.SetFactoryBean;
import org.springframework.beans.bean.definition.AbstractBeanDefinition;
import org.springframework.beans.bean.definition.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;

public class UtilNamespaceHandler extends NamespaceHandlerSupport {

	private static final String SCOPE_ATTRIBUTE = "scope";

	@Override
	public void init() {
		registerBeanDefinitionParser("constant", new ConstantBeanDefinitionParser());
		registerBeanDefinitionParser("property-path", new PropertyPathBeanDefinitionParser());
		registerBeanDefinitionParser("list", new ListBeanDefinitionParser());
		registerBeanDefinitionParser("set", new SetBeanDefinitionParser());
		registerBeanDefinitionParser("map", new MapBeanDefinitionParser());
		registerBeanDefinitionParser("properties", new PropertiesBeanDefinitionParser());
	}

	//Bean定义解析器(constant)
	private static class ConstantBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

		@Override
		protected Class<?> getBeanClass(Element element) {
			return FieldRetrievingFactoryBean.class;
		}

		@Override
		protected String resolveId(Element element, AbstractBeanDefinition definition, org.springframework.beans.factory.xml.parser.ParserContext parserContext) {
			String id = super.resolveId(element, definition, parserContext);
			if (!StringUtils.hasText(id)) {
				id = element.getAttribute("static-field");
			}
			return id;
		}
	}

	//Bean定义解析器(property-path)
	private static class PropertyPathBeanDefinitionParser extends org.springframework.beans.factory.xml.parser.AbstractSingleBeanDefinitionParser {

		@Override
		protected Class<?> getBeanClass(Element element) {
			return PropertyPathFactoryBean.class;
		}

		@Override
		protected void doParse(Element element, org.springframework.beans.factory.xml.parser.ParserContext parserContext, BeanDefinitionBuilder builder) {
			String path = element.getAttribute("path");
			if (!StringUtils.hasText(path)) {
				parserContext.getReaderContext().error("Attribute 'path' must not be empty", element);
				return;
			}
			int dotIndex = path.indexOf(".");
			if (dotIndex == -1) {
				parserContext.getReaderContext().error("Attribute 'path' must follow pattern 'beanName.propertyName'", element);
				return;
			}
			String beanName = path.substring(0, dotIndex);
			String propertyPath = path.substring(dotIndex + 1);
			builder.addPropertyValue("targetBeanName", beanName);
			builder.addPropertyValue("propertyPath", propertyPath);
		}

		@Override
		protected String resolveId(Element element, AbstractBeanDefinition definition, org.springframework.beans.factory.xml.parser.ParserContext parserContext) {
			String id = super.resolveId(element, definition, parserContext);
			if (!StringUtils.hasText(id)) {
				id = element.getAttribute("path");
			}
			return id;
		}
	}

	//Bean定义解析器(list)
	private static class ListBeanDefinitionParser extends org.springframework.beans.factory.xml.parser.AbstractSingleBeanDefinitionParser {

		@Override
		protected Class<?> getBeanClass(Element element) {
			return ListFactoryBean.class;
		}

		@Override
		protected void doParse(Element element, org.springframework.beans.factory.xml.parser.ParserContext parserContext, BeanDefinitionBuilder builder) {
			List<Object> parsedList = parserContext.getDelegate().parseListElement(element, builder.getRawBeanDefinition());
			builder.addPropertyValue("sourceList", parsedList);

			String listClass = element.getAttribute("list-class");
			if (StringUtils.hasText(listClass)) {
				builder.addPropertyValue("targetListClass", listClass);
			}

			String scope = element.getAttribute(SCOPE_ATTRIBUTE);
			if (StringUtils.hasLength(scope)) {
				builder.setScope(scope);
			}
		}
	}

	//Bean定义解析器(set)
	private static class SetBeanDefinitionParser extends org.springframework.beans.factory.xml.parser.AbstractSingleBeanDefinitionParser {

		@Override
		protected Class<?> getBeanClass(Element element) {
			return SetFactoryBean.class;
		}

		@Override
		protected void doParse(Element element, org.springframework.beans.factory.xml.parser.ParserContext parserContext, BeanDefinitionBuilder builder) {
			Set<Object> parsedSet = parserContext.getDelegate().parseSetElement(element, builder.getRawBeanDefinition());
			builder.addPropertyValue("sourceSet", parsedSet);

			String setClass = element.getAttribute("set-class");
			if (StringUtils.hasText(setClass)) {
				builder.addPropertyValue("targetSetClass", setClass);
			}

			String scope = element.getAttribute(SCOPE_ATTRIBUTE);
			if (StringUtils.hasLength(scope)) {
				builder.setScope(scope);
			}
		}
	}

	//Bean定义解析器(map)
	private static class MapBeanDefinitionParser extends org.springframework.beans.factory.xml.parser.AbstractSingleBeanDefinitionParser {

		@Override
		protected Class<?> getBeanClass(Element element) {
			return MapFactoryBean.class;
		}

		@Override
		protected void doParse(Element element, org.springframework.beans.factory.xml.parser.ParserContext parserContext, BeanDefinitionBuilder builder) {
			Map<Object, Object> parsedMap = parserContext.getDelegate().parseMapElement(element, builder.getRawBeanDefinition());
			builder.addPropertyValue("sourceMap", parsedMap);

			String mapClass = element.getAttribute("map-class");
			if (StringUtils.hasText(mapClass)) {
				builder.addPropertyValue("targetMapClass", mapClass);
			}

			String scope = element.getAttribute(SCOPE_ATTRIBUTE);
			if (StringUtils.hasLength(scope)) {
				builder.setScope(scope);
			}
		}
	}

	//Bean定义解析器(properties)
	private static class PropertiesBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

		@Override
		protected Class<?> getBeanClass(Element element) {
			return PropertiesFactoryBean.class;
		}

		@Override
		protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
			Properties parsedProps = parserContext.getDelegate().parsePropsElement(element);
			builder.addPropertyValue("properties", parsedProps);

			String location = element.getAttribute("location");
			if (StringUtils.hasLength(location)) {
				location = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(location);
				String[] locations = StringUtils.commaDelimitedListToStringArray(location);
				builder.addPropertyValue("locations", locations);
			}

			builder.addPropertyValue("ignoreResourceNotFound", Boolean.valueOf(element.getAttribute("ignore-resource-not-found")));
			builder.addPropertyValue("localOverride", Boolean.valueOf(element.getAttribute("local-override")));

			String scope = element.getAttribute(SCOPE_ATTRIBUTE);
			if (StringUtils.hasLength(scope)) {
				builder.setScope(scope);
			}
		}
	}

}
