package org.springframework.context;

//应用事件发布者
public interface ApplicationEventPublisher {

	//发布事件
	void publishEvent(ApplicationEvent event);

	//发布事件
	void publishEvent(Object event);

}
