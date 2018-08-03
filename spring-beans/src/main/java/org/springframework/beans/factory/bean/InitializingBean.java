package org.springframework.beans.factory.bean;

//初始化中的Bean
public interface InitializingBean {

	void afterPropertiesSet() throws Exception;

}
