package org.springframework.web.servlet.handler;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

//可匹配的处理器映射
public interface MatchableHandlerMapping extends HandlerMapping {

	//匹配方法
	RequestMatchResult match(HttpServletRequest request, String pattern);

}
