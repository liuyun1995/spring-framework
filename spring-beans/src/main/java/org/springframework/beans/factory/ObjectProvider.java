package org.springframework.beans.factory;

import org.springframework.beans.exception.BeansException;

//对象提供者
public interface ObjectProvider<T> extends ObjectFactory<T> {

	T getObject(Object... args) throws BeansException;

	T getIfAvailable() throws BeansException;

	T getIfUnique() throws BeansException;

}
