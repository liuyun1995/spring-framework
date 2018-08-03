package org.springframework.beans.factory.bean;

//一次性Bean
public interface DisposableBean {

	//销毁方法
	void destroy() throws Exception;

}
