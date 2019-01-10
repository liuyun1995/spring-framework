package org.springframework.web.servlet.mvc.method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.WebContentGenerator;

//抽象处理方法适配器
public abstract class AbstractHandlerMethodAdapter extends WebContentGenerator implements HandlerAdapter, Ordered {

	private int order = Ordered.LOWEST_PRECEDENCE;

	//构造器
	public AbstractHandlerMethodAdapter() {
		super(false);
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

	//是否支持
	@Override
	public final boolean supports(Object handler) {
		return (handler instanceof HandlerMethod && supportsInternal((HandlerMethod) handler));
	}

	//内部是否支持方法
	protected abstract boolean supportsInternal(HandlerMethod handlerMethod);

	//处理方法
	@Override
	public final ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		return handleInternal(request, response, (HandlerMethod) handler);
	}

	//内部处理方法
	protected abstract ModelAndView handleInternal(HttpServletRequest request,
			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception;

	//获取最后修改时间戳
	@Override
	public final long getLastModified(HttpServletRequest request, Object handler) {
		return getLastModifiedInternal(request, (HandlerMethod) handler);
	}

	//内部获取最后修改时间戳
	protected abstract long getLastModifiedInternal(HttpServletRequest request, HandlerMethod handlerMethod);

}
