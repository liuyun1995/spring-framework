package org.springframework.context.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

//应用上下文事件
@SuppressWarnings("serial")
public abstract class ApplicationContextEvent extends ApplicationEvent {
	
	//构造器
	public ApplicationContextEvent(ApplicationContext source) {
		super(source);
	}

	//获取应用上下文
	public final ApplicationContext getApplicationContext() {
		return (ApplicationContext) getSource();
	}

}
