package org.springframework.context;

import java.util.EventListener;

//应用事件监听者
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

	//处理一个应用事件
	void onApplicationEvent(E event);

}
