package org.springframework.context.event;

import org.springframework.context.ApplicationContext;

//上下文开启事件
@SuppressWarnings("serial")
public class ContextStartedEvent extends ApplicationContextEvent {

	public ContextStartedEvent(ApplicationContext source) {
		super(source);
	}

}
