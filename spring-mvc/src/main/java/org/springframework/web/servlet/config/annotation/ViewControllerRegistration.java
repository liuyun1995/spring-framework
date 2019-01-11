package org.springframework.web.servlet.config.annotation;

import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

//视图控制器登记处
public class ViewControllerRegistration {

	private final String urlPath;

	private final ParameterizableViewController controller = new ParameterizableViewController();

	//构造器
	public ViewControllerRegistration(String urlPath) {
		Assert.notNull(urlPath, "'urlPath' is required.");
		this.urlPath = urlPath;
	}

	//设置状态码
	public ViewControllerRegistration setStatusCode(HttpStatus statusCode) {
		this.controller.setStatusCode(statusCode);
		return this;
	}

	//设置视图名
	public void setViewName(String viewName) {
		this.controller.setViewName(viewName);
	}

	//设置应用上下文
	protected void setApplicationContext(ApplicationContext applicationContext) {
		this.controller.setApplicationContext(applicationContext);
	}

	//设置URL路径
	protected String getUrlPath() {
		return this.urlPath;
	}

	//获取视图控制器
	protected ParameterizableViewController getViewController() {
		return this.controller;
	}

}
