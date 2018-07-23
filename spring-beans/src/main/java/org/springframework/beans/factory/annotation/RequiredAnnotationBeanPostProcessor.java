package org.springframework.beans.factory.annotation;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Conventions;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

public class RequiredAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
        implements MergedBeanDefinitionPostProcessor, PriorityOrdered, BeanFactoryAware {

    /**
     * Bean definition attribute that may indicate whether a given bean is supposed
     * to be skipped when performing this post-processor's required property check.
     *
     * @see #shouldSkip
     */
    public static final String SKIP_REQUIRED_CHECK_ATTRIBUTE =
            Conventions.getQualifiedAttributeName(RequiredAnnotationBeanPostProcessor.class, "skipRequiredCheck");


    private Class<? extends Annotation> requiredAnnotationType = Required.class;

    private int order = Ordered.LOWEST_PRECEDENCE - 1;

    private ConfigurableListableBeanFactory beanFactory;

    /**
     * Cache for validated bean names, skipping re-validation for the same bean
     */
    private final Set<String> validatedBeanNames =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(64));


    /**
     * Set the 'required' annotation type, to be used on bean property
     * setter methods.
     * <p>The default required annotation type is the Spring-provided
     * {@link Required} annotation.
     * <p>This setter property exists so that developers can provide their own
     * (non-Spring-specific) annotation type to indicate that a property value
     * is required.
     */
    public void setRequiredAnnotationType(Class<? extends Annotation> requiredAnnotationType) {
        Assert.notNull(requiredAnnotationType, "'requiredAnnotationType' must not be null");
        this.requiredAnnotationType = requiredAnnotationType;
    }

    /**
     * Return the 'required' annotation type.
     */
    protected Class<? extends Annotation> getRequiredAnnotationType() {
        return this.requiredAnnotationType;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        }
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }


    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
    }

    @Override
    public PropertyValues postProcessPropertyValues(
            PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {

        if (!this.validatedBeanNames.contains(beanName)) {
            if (!shouldSkip(this.beanFactory, beanName)) {
                List<String> invalidProperties = new ArrayList<String>();
                for (PropertyDescriptor pd : pds) {
                    if (isRequiredProperty(pd) && !pvs.contains(pd.getName())) {
                        invalidProperties.add(pd.getName());
                    }
                }
                if (!invalidProperties.isEmpty()) {
                    throw new BeanInitializationException(buildExceptionMessage(invalidProperties, beanName));
                }
            }
            this.validatedBeanNames.add(beanName);
        }
        return pvs;
    }

    /**
     * Check whether the given bean definition is not subject to the annotation-based
     * required property check as performed by this post-processor.
     * <p>The default implementations check for the presence of the
     * {@link #SKIP_REQUIRED_CHECK_ATTRIBUTE} attribute in the bean definition, if any.
     * It also suggests skipping in case of a bean definition with a "factory-bean"
     * reference set, assuming that instance-based factories pre-populate the bean.
     *
     * @param beanFactory the BeanFactory to check against
     * @param beanName    the name of the bean to check against
     * @return {@code true} to skip the bean; {@code false} to process it
     */
    protected boolean shouldSkip(ConfigurableListableBeanFactory beanFactory, String beanName) {
        if (beanFactory == null || !beanFactory.containsBeanDefinition(beanName)) {
            return false;
        }
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        if (beanDefinition.getFactoryBeanName() != null) {
            return true;
        }
        Object value = beanDefinition.getAttribute(SKIP_REQUIRED_CHECK_ATTRIBUTE);
        return (value != null && (Boolean.TRUE.equals(value) || Boolean.valueOf(value.toString())));
    }

    /**
     * Is the supplied property required to have a value (that is, to be dependency-injected)?
     * <p>This implementation looks for the existence of a
     * {@link #setRequiredAnnotationType "required" annotation}
     * on the supplied {@link PropertyDescriptor property}.
     *
     * @param propertyDescriptor the target PropertyDescriptor (never {@code null})
     * @return {@code true} if the supplied property has been marked as being required;
     * {@code false} if not, or if the supplied property does not have a setter method
     */
    protected boolean isRequiredProperty(PropertyDescriptor propertyDescriptor) {
        Method setter = propertyDescriptor.getWriteMethod();
        return (setter != null && AnnotationUtils.getAnnotation(setter, getRequiredAnnotationType()) != null);
    }

    /**
     * Build an exception message for the given list of invalid properties.
     *
     * @param invalidProperties the list of names of invalid properties
     * @param beanName          the name of the bean
     * @return the exception message
     */
    private String buildExceptionMessage(List<String> invalidProperties, String beanName) {
        int size = invalidProperties.size();
        StringBuilder sb = new StringBuilder();
        sb.append(size == 1 ? "Property" : "Properties");
        for (int i = 0; i < size; i++) {
            String propertyName = invalidProperties.get(i);
            if (i > 0) {
                if (i == (size - 1)) {
                    sb.append(" and");
                } else {
                    sb.append(",");
                }
            }
            sb.append(" '").append(propertyName).append("'");
        }
        sb.append(size == 1 ? " is" : " are");
        sb.append(" required for bean '").append(beanName).append("'");
        return sb.toString();
    }

}
