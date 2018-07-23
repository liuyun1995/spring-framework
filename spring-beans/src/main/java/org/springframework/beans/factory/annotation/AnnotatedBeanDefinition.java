package org.springframework.beans.factory.annotation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;

public interface AnnotatedBeanDefinition extends BeanDefinition {

	AnnotationMetadata getMetadata();

	MethodMetadata getFactoryMethodMetadata();

}
