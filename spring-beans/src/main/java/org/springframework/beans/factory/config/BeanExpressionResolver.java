package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

//Bean表达式转换器
public interface BeanExpressionResolver {

	//计算方法
	Object evaluate(String value, BeanExpressionContext evalContext) throws BeansException;

}
