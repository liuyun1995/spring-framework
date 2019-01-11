package org.springframework.web.servlet.config.annotation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

//视图控制器注册
public class ViewControllerRegistry {

	private ApplicationContext applicationContext;

	private final List<ViewControllerRegistration> registrations = new ArrayList<ViewControllerRegistration>(4);

	private final List<RedirectViewControllerRegistration> redirectRegistrations = new ArrayList<RedirectViewControllerRegistration>(10);

	private int order = 1;

	//构造器
	@Deprecated
	public ViewControllerRegistry() {}

	//构造器
	public ViewControllerRegistry(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	//添加视图控制器
	public ViewControllerRegistration addViewController(String urlPath) {
		ViewControllerRegistration registration = new ViewControllerRegistration(urlPath);
		registration.setApplicationContext(this.applicationContext);
		this.registrations.add(registration);
		return registration;
	}

	//添加重定向视图控制器
	public RedirectViewControllerRegistration addRedirectViewController(String urlPath, String redirectUrl) {
		RedirectViewControllerRegistration registration = new RedirectViewControllerRegistration(urlPath, redirectUrl);
		registration.setApplicationContext(this.applicationContext);
		this.redirectRegistrations.add(registration);
		return registration;
	}

	//添加状态控制器
	public void addStatusController(String urlPath, HttpStatus statusCode) {
		ViewControllerRegistration registration = new ViewControllerRegistration(urlPath);
		registration.setApplicationContext(this.applicationContext);
		registration.setStatusCode(statusCode);
		registration.getViewController().setStatusOnly(true);
		this.registrations.add(registration);
	}

	//设置排序
	public void setOrder(int order) {
		this.order = order;
	}

	//绑定处理器映射
	protected SimpleUrlHandlerMapping buildHandlerMapping() {
		if (this.registrations.isEmpty() && this.redirectRegistrations.isEmpty()) {
			return null;
		}

		Map<String, Object> urlMap = new LinkedHashMap<String, Object>();
		for (ViewControllerRegistration registration : this.registrations) {
			urlMap.put(registration.getUrlPath(), registration.getViewController());
		}
		for (RedirectViewControllerRegistration registration : this.redirectRegistrations) {
			urlMap.put(registration.getUrlPath(), registration.getViewController());
		}

		SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
		handlerMapping.setUrlMap(urlMap);
		handlerMapping.setOrder(this.order);
		return handlerMapping;
	}

	//获取处理器映射
	@Deprecated
	protected AbstractHandlerMapping getHandlerMapping() {
		return buildHandlerMapping();
	}

	//设置应用上下文
	@Deprecated
	protected void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

}
