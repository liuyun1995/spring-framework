package org.springframework.context;

import java.util.EventObject;

public abstract class ApplicationEvent extends EventObject {

	private static final long serialVersionUID = 7099057708183571937L;

	//事件发生时间戳
	private final long timestamp;
	
	//构造器
	public ApplicationEvent(Object source) {
		super(source);
		this.timestamp = System.currentTimeMillis();
	}
	
	//获取事件发生时间戳
	public final long getTimestamp() {
		return this.timestamp;
	}

}
