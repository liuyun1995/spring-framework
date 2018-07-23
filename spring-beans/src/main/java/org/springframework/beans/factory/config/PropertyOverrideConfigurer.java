package org.springframework.beans.factory.config;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanInitializationException;

//属性覆盖配置器
public class PropertyOverrideConfigurer extends PropertyResourceConfigurer {

    public static final String DEFAULT_BEAN_NAME_SEPARATOR = ".";

    private String beanNameSeparator = DEFAULT_BEAN_NAME_SEPARATOR;

    private boolean ignoreInvalidKeys = false;

    private final Set<String> beanNames = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(16));

    public void setBeanNameSeparator(String beanNameSeparator) {
        this.beanNameSeparator = beanNameSeparator;
    }

    public void setIgnoreInvalidKeys(boolean ignoreInvalidKeys) {
        this.ignoreInvalidKeys = ignoreInvalidKeys;
    }

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
            throws BeansException {

        for (Enumeration<?> names = props.propertyNames(); names.hasMoreElements(); ) {
            String key = (String) names.nextElement();
            try {
                processKey(beanFactory, key, props.getProperty(key));
            } catch (BeansException ex) {
                String msg = "Could not process key '" + key + "' in PropertyOverrideConfigurer";
                if (!this.ignoreInvalidKeys) {
                    throw new BeanInitializationException(msg, ex);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug(msg, ex);
                }
            }
        }
    }

    protected void processKey(ConfigurableListableBeanFactory factory, String key, String value)
            throws BeansException {

        int separatorIndex = key.indexOf(this.beanNameSeparator);
        if (separatorIndex == -1) {
            throw new BeanInitializationException("Invalid key '" + key +
                    "': expected 'beanName" + this.beanNameSeparator + "property'");
        }
        String beanName = key.substring(0, separatorIndex);
        String beanProperty = key.substring(separatorIndex + 1);
        this.beanNames.add(beanName);
        applyPropertyValue(factory, beanName, beanProperty, value);
        if (logger.isDebugEnabled()) {
            logger.debug("Property '" + key + "' set to value [" + value + "]");
        }
    }

    protected void applyPropertyValue(
            ConfigurableListableBeanFactory factory, String beanName, String property, String value) {

        BeanDefinition bd = factory.getBeanDefinition(beanName);
        while (bd.getOriginatingBeanDefinition() != null) {
            bd = bd.getOriginatingBeanDefinition();
        }
        PropertyValue pv = new PropertyValue(property, value);
        pv.setOptional(this.ignoreInvalidKeys);
        bd.getPropertyValues().addPropertyValue(pv);
    }

    public boolean hasPropertyOverridesFor(String beanName) {
        return this.beanNames.contains(beanName);
    }

}
