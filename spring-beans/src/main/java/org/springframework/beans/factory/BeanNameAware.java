package org.springframework.beans.factory;

//Bean名称装配器
public interface BeanNameAware extends Aware {
	
	void setBeanName(String name);

}
