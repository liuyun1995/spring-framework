package org.springframework.beans.factory.xml;

import org.w3c.dom.Document;

import org.springframework.beans.factory.BeanDefinitionStoreException;

//Bean定义文档解析器
public interface BeanDefinitionDocumentReader {

	//注册Bean定义
	void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) throws BeanDefinitionStoreException;

}
