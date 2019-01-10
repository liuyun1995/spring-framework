package org.springframework.web.servlet.handler;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

//处理异常解析器混合
public class HandlerExceptionResolverComposite implements HandlerExceptionResolver, Ordered {

	private List<HandlerExceptionResolver> resolvers;

	private int order = Ordered.LOWEST_PRECEDENCE;

	//设置异常解析器
	public void setExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
		this.resolvers = exceptionResolvers;
	}

	//获取异常解析器
	public List<HandlerExceptionResolver> getExceptionResolvers() {
		return (this.resolvers != null ? Collections.unmodifiableList(this.resolvers) :
				Collections.<HandlerExceptionResolver>emptyList());
	}

	//设置排序
	public void setOrder(int order) {
		this.order = order;
	}

	//获取排序
	@Override
	public int getOrder() {
		return this.order;
	}

	//解析异常
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
			Object handler,Exception ex) {

		if (this.resolvers != null) {
			for (HandlerExceptionResolver handlerExceptionResolver : this.resolvers) {
				ModelAndView mav = handlerExceptionResolver.resolveException(request, response, handler, ex);
				if (mav != null) {
					return mav;
				}
			}
		}
		return null;
	}

}
