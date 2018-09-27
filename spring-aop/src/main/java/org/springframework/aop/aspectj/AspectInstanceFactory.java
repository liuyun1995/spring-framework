package org.springframework.aop.aspectj;

import org.springframework.core.Ordered;

//切面实例工厂
public interface AspectInstanceFactory extends Ordered {

	//获取切面实例
	Object getAspectInstance();

	//获取切面类加载器
	ClassLoader getAspectClassLoader();

}
