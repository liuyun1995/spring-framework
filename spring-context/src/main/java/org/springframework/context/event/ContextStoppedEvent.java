package org.springframework.context.event;

import org.springframework.context.ApplicationContext;

//上下文停止事件
@SuppressWarnings("serial")
public class ContextStoppedEvent extends ApplicationContextEvent {
	
	public ContextStoppedEvent(ApplicationContext source) {
		super(source);
	}

}
