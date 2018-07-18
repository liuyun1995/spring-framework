package org.springframework.web.servlet;

import java.util.Locale;

//视图解析器
public interface ViewResolver {

	//通过名称解析视图
	View resolveViewName(String viewName, Locale locale) throws Exception;

}
