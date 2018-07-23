package org.springframework.beans.factory.access;

import org.springframework.beans.BeansException;

//Bean工厂定位器
public interface BeanFactoryLocator {

	BeanFactoryReference useBeanFactory(String factoryKey) throws BeansException;

}
