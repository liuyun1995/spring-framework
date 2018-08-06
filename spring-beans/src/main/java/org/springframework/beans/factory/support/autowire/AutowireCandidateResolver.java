package org.springframework.beans.factory.support.autowire;

import org.springframework.beans.factory.bean.definition.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;

//自动装配候选者解析器
public interface AutowireCandidateResolver {

	boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor);

	Object getSuggestedValue(DependencyDescriptor descriptor);

	Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, String beanName);

}
