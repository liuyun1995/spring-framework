package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

//控制器
public interface Controller {

	//处理请求
	ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
