package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;

//请求到视图名称转换器
public interface RequestToViewNameTranslator {

	//获取视图名称
	String getViewName(HttpServletRequest request) throws Exception;

}
