package org.springframework.beans.factory.xml;

import org.springframework.beans.factory.parsing.DefaultsDefinition;

//文档默认定义
public class DocumentDefaultsDefinition implements DefaultsDefinition {

	private String lazyInit;
	private String merge;
	private String autowire;
	private String dependencyCheck;
	private String autowireCandidates;
	private String initMethod;
	private String destroyMethod;
	private Object source;

	//设置懒初始化
	public void setLazyInit(String lazyInit) {
		this.lazyInit = lazyInit;
	}

	//获取懒初始化
	public String getLazyInit() {
		return this.lazyInit;
	}

	//设置是否合并
	public void setMerge(String merge) {
		this.merge = merge;
	}

	//获取是否合并
	public String getMerge() {
		return this.merge;
	}

	//设置自动装配
	public void setAutowire(String autowire) {
		this.autowire = autowire;
	}

	//获取自动装配
	public String getAutowire() {
		return this.autowire;
	}

	//设置依赖检查
	public void setDependencyCheck(String dependencyCheck) {
		this.dependencyCheck = dependencyCheck;
	}

	//获取依赖检查
	public String getDependencyCheck() {
		return this.dependencyCheck;
	}

	//设置自动装配候选
	public void setAutowireCandidates(String autowireCandidates) {
		this.autowireCandidates = autowireCandidates;
	}

	//获取自动装配候选
	public String getAutowireCandidates() {
		return this.autowireCandidates;
	}

	//设置初始方法
	public void setInitMethod(String initMethod) {
		this.initMethod = initMethod;
	}

	//获取初始方法
	public String getInitMethod() {
		return this.initMethod;
	}

	//设置销毁方法
	public void setDestroyMethod(String destroyMethod) {
		this.destroyMethod = destroyMethod;
	}

	//获取销毁方法
	public String getDestroyMethod() {
		return this.destroyMethod;
	}

	//设置资源对象
	public void setSource(Object source) {
		this.source = source;
	}

	//获取资源对象
	@Override
	public Object getSource() {
		return this.source;
	}

}
