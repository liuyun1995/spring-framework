package org.springframework.context.event;

import org.springframework.context.ApplicationContext;

//上下文关闭事件
@SuppressWarnings("serial")
public class ContextClosedEvent extends ApplicationContextEvent {
	
	public ContextClosedEvent(ApplicationContext source) {
		super(source);
	}

}
