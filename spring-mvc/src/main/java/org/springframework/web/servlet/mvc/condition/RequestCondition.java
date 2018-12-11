package org.springframework.web.servlet.mvc.condition;

import javax.servlet.http.HttpServletRequest;

//请求表达式
public interface RequestCondition<T> {

	//联合方法
	T combine(T other);

	//获取匹配的表达式
	T getMatchingCondition(HttpServletRequest request);

	//表达式比较
	int compareTo(T other, HttpServletRequest request);

}
