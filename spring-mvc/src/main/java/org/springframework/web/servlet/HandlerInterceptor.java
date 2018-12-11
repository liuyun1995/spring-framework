package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//处理器拦截器
public interface HandlerInterceptor {

	//处理前执行
	boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception;

	//处理后执行
	void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
					ModelAndView modelAndView) throws Exception;

	//完成后执行
	void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
						 Exception ex) throws Exception;

}
