package org.springframework.web.servlet.config.annotation;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

//拦截器注册器
public class InterceptorRegistry {

	private final List<InterceptorRegistration> registrations = new ArrayList<InterceptorRegistration>();

	//添加拦截器
	public InterceptorRegistration addInterceptor(HandlerInterceptor interceptor) {
		InterceptorRegistration registration = new InterceptorRegistration(interceptor);
		this.registrations.add(registration);
		return registration;
	}

	//添加Web请求拦截器
	public InterceptorRegistration addWebRequestInterceptor(WebRequestInterceptor interceptor) {
		WebRequestHandlerInterceptorAdapter adapted = new WebRequestHandlerInterceptorAdapter(interceptor);
		InterceptorRegistration registration = new InterceptorRegistration(adapted);
		this.registrations.add(registration);
		return registration;
	}

	//获取所有拦截器
	protected List<Object> getInterceptors() {
		List<Object> interceptors = new ArrayList<Object>(this.registrations.size());
		for (InterceptorRegistration registration : this.registrations) {
			interceptors.add(registration.getInterceptor());
		}
		return interceptors ;
	}

}
