package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;

public interface AutowireCandidateResolver {

	boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor);

	Object getSuggestedValue(DependencyDescriptor descriptor);

	Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, String beanName);

}
