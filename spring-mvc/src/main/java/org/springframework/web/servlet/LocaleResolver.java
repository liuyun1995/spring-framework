package org.springframework.web.servlet;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//Locale解析器
public interface LocaleResolver {

	//解析Locale
	Locale resolveLocale(HttpServletRequest request);

	//设置Locale
	void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale);

}
