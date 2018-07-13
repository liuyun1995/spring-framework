package org.springframework.context;

//智能生命周期
public interface SmartLifecycle extends Lifecycle, Phased {

	//是否自动启动
	boolean isAutoStartup();

	//停止
	void stop(Runnable callback);

}
