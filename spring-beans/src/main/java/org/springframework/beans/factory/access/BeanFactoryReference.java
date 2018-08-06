package org.springframework.beans.factory.access;

import org.springframework.beans.factory.BeanFactory;

//Bean工厂引用
public interface BeanFactoryReference {

	//获取Bean工厂
	BeanFactory getFactory();

	//释放引用
	void release();

}
