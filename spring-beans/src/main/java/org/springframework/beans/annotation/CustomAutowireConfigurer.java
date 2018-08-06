package org.springframework.beans.annotation;

import java.lang.annotation.Annotation;
import java.util.Set;
import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.support.autowire.BeanClassLoaderAware;
import org.springframework.beans.factory.support.processor.BeanFactoryPostProcessor;
import org.springframework.beans.factory.ConfigurableListableBeanFactory;
import org.springframework.beans.bean.registry.DefaultListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

//外部自动装配配置器
public class CustomAutowireConfigurer implements BeanFactoryPostProcessor, BeanClassLoaderAware, Ordered {

    private int order = Ordered.LOWEST_PRECEDENCE;
    private Set<?> customQualifierTypes;
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;
    }

    public void setCustomQualifierTypes(Set<?> customQualifierTypes) {
        this.customQualifierTypes = customQualifierTypes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (this.customQualifierTypes != null) {
            if (!(beanFactory instanceof DefaultListableBeanFactory)) {
                throw new IllegalStateException(
                        "CustomAutowireConfigurer needs to operate on a DefaultListableBeanFactory");
            }
            DefaultListableBeanFactory dlbf = (DefaultListableBeanFactory) beanFactory;
            if (!(dlbf.getAutowireCandidateResolver() instanceof org.springframework.beans.annotation.QualifierAnnotationAutowireCandidateResolver)) {
                dlbf.setAutowireCandidateResolver(new org.springframework.beans.annotation.QualifierAnnotationAutowireCandidateResolver());
            }
            org.springframework.beans.annotation.QualifierAnnotationAutowireCandidateResolver resolver =
                    (QualifierAnnotationAutowireCandidateResolver) dlbf.getAutowireCandidateResolver();
            for (Object value : this.customQualifierTypes) {
                Class<? extends Annotation> customType = null;
                if (value instanceof Class) {
                    customType = (Class<? extends Annotation>) value;
                } else if (value instanceof String) {
                    String className = (String) value;
                    customType = (Class<? extends Annotation>) ClassUtils.resolveClassName(className, this.beanClassLoader);
                } else {
                    throw new IllegalArgumentException(
                            "Invalid value [" + value + "] for custom qualifier type: needs to be Class or String.");
                }
                if (!Annotation.class.isAssignableFrom(customType)) {
                    throw new IllegalArgumentException(
                            "Qualifier type [" + customType.getName() + "] needs to be annotation type");
                }
                resolver.addQualifierType(customType);
            }
        }
    }

}
