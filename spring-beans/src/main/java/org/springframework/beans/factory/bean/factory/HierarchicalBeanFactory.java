package org.springframework.beans.factory.bean.factory;

//分级Bean工厂
public interface HierarchicalBeanFactory extends BeanFactory {

	//获取父类Bean工厂
	BeanFactory getParentBeanFactory();

	//是否包含指定Bean
	boolean containsLocalBean(String name);

}
