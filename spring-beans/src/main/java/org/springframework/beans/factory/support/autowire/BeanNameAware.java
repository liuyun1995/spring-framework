package org.springframework.beans.factory.support.autowire;

//Bean名称装配器
public interface BeanNameAware extends Aware {

	//设置Bean名称
	void setBeanName(String name);

}