package org.springframework.beans.factory.xml;

import org.w3c.dom.Document;

import org.springframework.beans.factory.BeanDefinitionStoreException;

//Bean定义文档阅读器
public interface BeanDefinitionDocumentReader {

	//根据Document对象来注册Bean定义
	void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) throws BeanDefinitionStoreException;

}
