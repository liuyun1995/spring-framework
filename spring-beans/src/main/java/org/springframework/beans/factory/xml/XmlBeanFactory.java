package org.springframework.beans.factory.xml;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.Resource;

//通过XML创建BeanFactory
@Deprecated
@SuppressWarnings({"serial", "all"})
public class XmlBeanFactory extends DefaultListableBeanFactory {

	//Bean定义阅读器
	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);

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
