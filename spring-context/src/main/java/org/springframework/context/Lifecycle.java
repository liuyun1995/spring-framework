package org.springframework.context;

//生命周期
public interface Lifecycle {

	//开启组件
	void start();

	//停止组件
	void stop();

	//是否运行
	boolean isRunning();

}
