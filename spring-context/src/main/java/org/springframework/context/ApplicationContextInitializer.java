package org.springframework.context;

//应用上下文初始器
public interface ApplicationContextInitializer<C extends ConfigurableApplicationContext> {
	
	void initialize(C applicationContext);

}
