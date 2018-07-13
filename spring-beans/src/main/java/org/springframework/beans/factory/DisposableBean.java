package org.springframework.beans.factory;

//一次性Bean
public interface DisposableBean {

	void destroy() throws Exception;

}
