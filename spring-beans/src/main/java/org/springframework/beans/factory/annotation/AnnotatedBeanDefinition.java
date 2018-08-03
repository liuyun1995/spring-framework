package org.springframework.beans.factory.annotation;

import org.springframework.beans.factory.bean.definition.BeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;

//注解的Bean定义
public interface AnnotatedBeanDefinition extends BeanDefinition {

	//获取注解元数据
	AnnotationMetadata getMetadata();

	//获取工厂方法元数据
	MethodMetadata getFactoryMethodMetadata();

}
