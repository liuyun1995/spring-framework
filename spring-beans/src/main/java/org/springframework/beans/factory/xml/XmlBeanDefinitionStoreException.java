package org.springframework.beans.factory.xml;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.springframework.beans.exception.BeanDefinitionStoreException;

@SuppressWarnings("serial")
public class XmlBeanDefinitionStoreException extends BeanDefinitionStoreException {

	public XmlBeanDefinitionStoreException(String resourceDescription, String msg, SAXException cause) {
		super(resourceDescription, msg, cause);
	}

	public int getLineNumber() {
		Throwable cause = getCause();
		if (cause instanceof SAXParseException) {
			return ((SAXParseException) cause).getLineNumber();
		}
		return -1;
	}

}
