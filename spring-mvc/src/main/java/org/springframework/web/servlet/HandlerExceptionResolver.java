package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//处理异常解决器
public interface HandlerExceptionResolver {

	//处理异常
	ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex);

}
