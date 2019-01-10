package org.springframework.web.servlet;

import org.springframework.context.i18n.LocaleContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//Locale上下文解析器
public interface LocaleContextResolver extends LocaleResolver {

	//解析Locale上下文
	LocaleContext resolveLocaleContext(HttpServletRequest request);

	//设置Locale上下文
	void setLocaleContext(HttpServletRequest request, HttpServletResponse response, LocaleContext localeContext);

}
