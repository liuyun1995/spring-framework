package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

public abstract class AbstractSingleBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected final AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        String parentName = getParentName(element);
        if (parentName != null) {
            builder.getRawBeanDefinition().setParentName(parentName);
        }
        Class<?> beanClass = getBeanClass(element);
        if (beanClass != null) {
            builder.getRawBeanDefinition().setBeanClass(beanClass);
        } else {
            String beanClassName = getBeanClassName(element);
            if (beanClassName != null) {
                builder.getRawBeanDefinition().setBeanClassName(beanClassName);
            }
        }
        builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
        if (parserContext.isNested()) {
            // Inner bean definition must receive same scope as containing bean.
            builder.setScope(parserContext.getContainingBeanDefinition().getScope());
        }
        if (parserContext.isDefaultLazyInit()) {
            // Default-lazy-init applies to custom bean definitions as well.
            builder.setLazyInit(true);
        }
        doParse(element, parserContext, builder);
        return builder.getBeanDefinition();
    }

    /**
     * Determine the name for the parent of the currently parsed bean,
     * in case of the current bean being defined as a child bean.
     * <p>The default implementation returns {@code null},
     * indicating a root bean definition.
     *
     * @param element the {@code Element} that is being parsed
     * @return the name of the parent bean for the currently parsed bean,
     * or {@code null} if none
     */
    protected String getParentName(Element element) {
        return null;
    }

    /**
     * Determine the bean class corresponding to the supplied {@link Element}.
     * <p>Note that, for application classes, it is generally preferable to
     * override {@link #getBeanClassName} instead, in order to avoid a direct
     * dependence on the bean implementation class. The BeanDefinitionParser
     * and its NamespaceHandler can be used within an IDE plugin then, even
     * if the application classes are not available on the plugin's classpath.
     *
     * @param element the {@code Element} that is being parsed
     * @return the {@link Class} of the bean that is being defined via parsing
     * the supplied {@code Element}, or {@code null} if none
     * @see #getBeanClassName
     */
    protected Class<?> getBeanClass(Element element) {
        return null;
    }

    protected String getBeanClassName(Element element) {
        return null;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        doParse(element, builder);
    }

    protected void doParse(Element element, BeanDefinitionBuilder builder) {}

}
