package org.springframework.beans.factory.access;

import org.springframework.beans.factory.bean.factory.BeanFactory;

//Bean工厂引用
public interface BeanFactoryReference {
	
	BeanFactory getFactory();

	void release();

}
