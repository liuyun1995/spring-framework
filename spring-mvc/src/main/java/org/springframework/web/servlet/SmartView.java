package org.springframework.web.servlet;

//智能视图
public interface SmartView extends View {

	//是否重定向视图
	boolean isRedirectView();

}
