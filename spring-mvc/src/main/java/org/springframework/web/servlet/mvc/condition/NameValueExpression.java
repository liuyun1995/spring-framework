package org.springframework.web.servlet.mvc.condition;

//键值对表达式
public interface NameValueExpression<T> {

	String getName();

	T getValue();

	boolean isNegated();

}
