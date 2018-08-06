package org.springframework.beans.factory.config;

import org.springframework.beans.factory.ConfigurableListableBeanFactory;
import org.springframework.beans.exception.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.autowire.BeanFactoryAware;
import org.springframework.beans.factory.support.autowire.BeanNameAware;
import org.springframework.beans.bean.definition.BeanDefinition;
import org.springframework.beans.bean.definition.BeanDefinitionVisitor;
import org.springframework.util.StringValueResolver;

//占位符配置器助手
public abstract class PlaceholderConfigurerSupport extends PropertyResourceConfigurer
        implements BeanNameAware, BeanFactoryAware {

    public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";     //默认前缀
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";      //默认后缀
    public static final String DEFAULT_VALUE_SEPARATOR = ":";         //默认分隔符
    protected String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;  //持有的前缀
    protected String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;  //持有的后缀
    protected String valueSeparator = DEFAULT_VALUE_SEPARATOR;        //持有的分隔符
    protected boolean trimValues = false;
    protected String nullValue;
    protected boolean ignoreUnresolvablePlaceholders = false;
    private String beanName;
    private BeanFactory beanFactory;


    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
    }

    public void setValueSeparator(String valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    public void setTrimValues(boolean trimValues) {
        this.trimValues = trimValues;
    }

    public void setNullValue(String nullValue) {
        this.nullValue = nullValue;
    }

    public void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders) {
        this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                       StringValueResolver valueResolver) {

        BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);

        String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
        for (String curName : beanNames) {
            if (!(curName.equals(this.beanName) && beanFactoryToProcess.equals(this.beanFactory))) {
                BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(curName);
                try {
                    visitor.visitBeanDefinition(bd);
                } catch (Exception ex) {
                    throw new BeanDefinitionStoreException(bd.getResourceDescription(), curName, ex.getMessage(), ex);
                }
            }
        }
        beanFactoryToProcess.resolveAliases(valueResolver);
        beanFactoryToProcess.addEmbeddedValueResolver(valueResolver);
    }

}
