package org.springframework.beans.factory.config;

import org.springframework.beans.exception.BeansException;

//Bean表达式解析器
public interface BeanExpressionResolver {

	//计算方法
	Object evaluate(String value, BeanExpressionContext evalContext) throws BeansException;

}
