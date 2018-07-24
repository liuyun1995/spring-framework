package org.springframework.aop.framework.autoproxy;

import org.springframework.aop.TargetSource;

public interface TargetSourceCreator {

	TargetSource getTargetSource(Class<?> beanClass, String beanName);

}
