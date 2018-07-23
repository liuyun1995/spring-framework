package org.springframework.beans.factory;

//智能初始化单例
public interface SmartInitializingSingleton {

	void afterSingletonsInstantiated();

}
