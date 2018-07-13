package org.springframework.beans.factory;

//初始化中的Bean
public interface InitializingBean {

	void afterPropertiesSet() throws Exception;

}
