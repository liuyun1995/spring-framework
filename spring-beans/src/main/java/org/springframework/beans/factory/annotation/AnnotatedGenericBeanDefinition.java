package org.springframework.beans.factory.annotation;

import org.springframework.beans.factory.bean.definition.GenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.util.Assert;

@SuppressWarnings("serial")
public class AnnotatedGenericBeanDefinition extends GenericBeanDefinition implements AnnotatedBeanDefinition {

    private final AnnotationMetadata metadata;

    private MethodMetadata factoryMethodMetadata;

    public AnnotatedGenericBeanDefinition(Class<?> beanClass) {
        setBeanClass(beanClass);
        this.metadata = new StandardAnnotationMetadata(beanClass, true);
    }

    public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata) {
        Assert.notNull(metadata, "AnnotationMetadata must not be null");
        if (metadata instanceof StandardAnnotationMetadata) {
            setBeanClass(((StandardAnnotationMetadata) metadata).getIntrospectedClass());
        } else {
            setBeanClassName(metadata.getClassName());
        }
        this.metadata = metadata;
    }

    public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata, MethodMetadata factoryMethodMetadata) {
        this(metadata);
        Assert.notNull(factoryMethodMetadata, "MethodMetadata must not be null");
        setFactoryMethodName(factoryMethodMetadata.getMethodName());
        this.factoryMethodMetadata = factoryMethodMetadata;
    }

    @Override
    public final AnnotationMetadata getMetadata() {
        return this.metadata;
    }

    @Override
    public final MethodMetadata getFactoryMethodMetadata() {
        return this.factoryMethodMetadata;
    }

}
