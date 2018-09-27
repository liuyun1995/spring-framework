package org.springframework.aop.framework.autoproxy;

import org.springframework.aop.TargetSource;

//目标源建立者
public interface TargetSourceCreator {

	TargetSource getTargetSource(Class<?> beanClass, String beanName);

}
