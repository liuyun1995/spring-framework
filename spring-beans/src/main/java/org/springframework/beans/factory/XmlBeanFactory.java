package org.springframework.beans.factory;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.reader.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;

//通过XML创建BeanFactory
@Deprecated
@SuppressWarnings({"serial", "all"})
public class XmlBeanFactory extends DefaultListableBeanFactory {

	//Bean定义阅读器
	private final org.springframework.beans.factory.xml.reader.XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);

	//构造器1
	public XmlBeanFactory(Resource resource) throws BeansException {
		this(resource, null);
	}

	//构造器2
	public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		this.reader.loadBeanDefinitions(resource);
	}

}
