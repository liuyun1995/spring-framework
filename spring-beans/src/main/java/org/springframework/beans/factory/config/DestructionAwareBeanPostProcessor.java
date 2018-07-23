package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

	void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException;

	boolean requiresDestruction(Object bean);

}
