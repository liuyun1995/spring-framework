package org.springframework.context;

//生命周期处理器
public interface LifecycleProcessor extends Lifecycle {

	void onRefresh();
	
	void onClose();

}
