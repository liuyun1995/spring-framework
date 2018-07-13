package org.springframework.context.event;

import org.springframework.context.ApplicationContext;

//上下文刷新事件
@SuppressWarnings("serial")
public class ContextRefreshedEvent extends ApplicationContextEvent {
	
	public ContextRefreshedEvent(ApplicationContext source) {
		super(source);
	}

}
