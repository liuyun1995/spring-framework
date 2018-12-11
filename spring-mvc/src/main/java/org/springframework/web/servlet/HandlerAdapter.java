package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//处理器适配器接口
public interface HandlerAdapter {

	/**
	 * Given a handler instance, return whether or not this {@code HandlerAdapter}
	 * can support it. Typical HandlerAdapters will base the decision on the handler
	 * type. HandlerAdapters will usually only support one handler type each.
	 * <p>A typical implementation:
	 * <p>{@code
	 * return (handler instanceof MyHandler);
	 * }
	 * @param handler handler object to check
	 * @return whether or not this object can use the given handler
	 */
	boolean supports(Object handler);

	/**
	 * Use the given handler to handle this request.
	 * The workflow that is required may vary widely.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler handler to use. This object must have previously been passed
	 * to the {@code supports} method of this interface, which must have
	 * returned {@code true}.
	 * @throws Exception in case of errors
	 * @return ModelAndView object with the name of the view and the required
	 * model data, or {@code null} if the request has been handled directly
	 */
	ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

	/**
	 * Same contract as for HttpServlet's {@code getLastModified} method.
	 * Can simply return -1 if there's no support in the handler class.
	 * @param request current HTTP request
	 * @param handler handler to use
	 * @return the lastModified value for the given handler
	 * @see javax.servlet.http.HttpServlet#getLastModified
	 * @see org.springframework.web.servlet.mvc.LastModified#getLastModified
	 */
	long getLastModified(HttpServletRequest request, Object handler);

}
