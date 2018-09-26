package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//主题解析器
public interface ThemeResolver {

	//解析主题名称
	String resolveThemeName(HttpServletRequest request);

	//设置主题名称
	void setThemeName(HttpServletRequest request, HttpServletResponse response, String themeName);

}
