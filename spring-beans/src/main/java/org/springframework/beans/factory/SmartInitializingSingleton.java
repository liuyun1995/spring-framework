package org.springframework.beans.factory;

//智能初始化单例
public interface SmartInitializingSingleton {

	//单例初始化之后执行
	void afterSingletonsInstantiated();

}
