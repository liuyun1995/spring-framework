package org.springframework.beans.support.processor;

import org.springframework.beans.exception.BeansException;

public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

	void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException;

	boolean requiresDestruction(Object bean);

}
