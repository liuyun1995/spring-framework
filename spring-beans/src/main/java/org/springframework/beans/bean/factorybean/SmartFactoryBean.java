package org.springframework.beans.bean.factorybean;

//智能工厂Bean
public interface SmartFactoryBean<T> extends FactoryBean<T> {

	//是否是原型
	boolean isPrototype();

	//是否急于初始化
	boolean isEagerInit();

}
