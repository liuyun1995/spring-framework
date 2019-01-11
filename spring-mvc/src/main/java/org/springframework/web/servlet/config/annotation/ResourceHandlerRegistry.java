package org.springframework.web.servlet.config.annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

//资源处理器注册器
public class ResourceHandlerRegistry {

	//Servlet上下文
	private final ServletContext servletContext;

	//应用上下文
	private final ApplicationContext applicationContext;

	private final ContentNegotiationManager contentNegotiationManager;

	private final List<ResourceHandlerRegistration> registrations = new ArrayList<ResourceHandlerRegistration>();

	private int order = Integer.MAX_VALUE -1;

	//构造器
	public ResourceHandlerRegistry(ApplicationContext applicationContext, ServletContext servletContext) {
		this(applicationContext, servletContext, null);
	}

	//构造器
	public ResourceHandlerRegistry(ApplicationContext applicationContext, ServletContext servletContext,
			ContentNegotiationManager contentNegotiationManager) {
		Assert.notNull(applicationContext, "ApplicationContext is required");
		this.applicationContext = applicationContext;
		this.servletContext = servletContext;
		this.contentNegotiationManager = contentNegotiationManager;
	}

	//添加资源处理器
	public ResourceHandlerRegistration addResourceHandler(String... pathPatterns) {
		ResourceHandlerRegistration registration =
				new ResourceHandlerRegistration(this.applicationContext, pathPatterns);
		this.registrations.add(registration);
		return registration;
	}

	/**
	 * Whether a resource handler has already been registered for the given path pattern.
	 */
	public boolean hasMappingForPattern(String pathPattern) {
		for (ResourceHandlerRegistration registration : this.registrations) {
			if (Arrays.asList(registration.getPathPatterns()).contains(pathPattern)) {
				return true;
			}
		}
		return false;
	}

	//设置排序
	public ResourceHandlerRegistry setOrder(int order) {
		this.order = order;
		return this;
	}

	//获取处理器映射
	protected AbstractHandlerMapping getHandlerMapping() {
		if (this.registrations.isEmpty()) {
			return null;
		}

		Map<String, HttpRequestHandler> urlMap = new LinkedHashMap<String, HttpRequestHandler>();
		for (ResourceHandlerRegistration registration : this.registrations) {
			for (String pathPattern : registration.getPathPatterns()) {
				ResourceHttpRequestHandler handler = registration.getRequestHandler();
				handler.setServletContext(this.servletContext);
				handler.setApplicationContext(this.applicationContext);
				handler.setContentNegotiationManager(this.contentNegotiationManager);
				try {
					handler.afterPropertiesSet();
				}
				catch (Throwable ex) {
					throw new BeanInitializationException("Failed to init ResourceHttpRequestHandler", ex);
				}
				urlMap.put(pathPattern, handler);
			}
		}

		SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
		handlerMapping.setOrder(order);
		handlerMapping.setUrlMap(urlMap);
		return handlerMapping;
	}

}
