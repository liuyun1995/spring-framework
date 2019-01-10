package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//处理器适配器接口
public interface HandlerAdapter {

	//是否支持该处理器
	boolean supports(Object handler);

	//使用给定处理器处理请求
	ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

	//获取最后修改的时间戳
	long getLastModified(HttpServletRequest request, Object handler);

}
