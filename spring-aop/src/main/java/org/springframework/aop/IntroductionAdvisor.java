package org.springframework.aop;

public interface IntroductionAdvisor extends Advisor, IntroductionInfo {

	//获取类型过滤器
	ClassFilter getClassFilter();

	//验证接口
	void validateInterfaces() throws IllegalArgumentException;

}
