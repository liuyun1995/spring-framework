package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;

//Bean引用
public interface BeanReference extends BeanMetadataElement {

	//获取Bean名称
	String getBeanName();

}
