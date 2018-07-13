package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

//对象工厂
public interface ObjectFactory<T> {

	T getObject() throws BeansException;

}
