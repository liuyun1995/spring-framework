package org.springframework.beans.factory.access;

import org.springframework.beans.factory.BeanFactory;

public interface BeanFactoryReference {
	
	BeanFactory getFactory();

	void release();

}
