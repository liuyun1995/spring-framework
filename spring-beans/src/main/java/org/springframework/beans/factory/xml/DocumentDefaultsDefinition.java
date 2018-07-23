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

	public void setMerge(String merge) {
		this.merge = merge;
	}

	public String getMerge() {
		return this.merge;
	}

	public void setAutowire(String autowire) {
		this.autowire = autowire;
	}

	public String getAutowire() {
		return this.autowire;
	}

	public void setDependencyCheck(String dependencyCheck) {
		this.dependencyCheck = dependencyCheck;
	}

	public String getDependencyCheck() {
		return this.dependencyCheck;
	}

	public void setAutowireCandidates(String autowireCandidates) {
		this.autowireCandidates = autowireCandidates;
	}

	public String getAutowireCandidates() {
		return this.autowireCandidates;
	}

	public void setInitMethod(String initMethod) {
		this.initMethod = initMethod;
	}

	public String getInitMethod() {
		return this.initMethod;
	}

	public void setDestroyMethod(String destroyMethod) {
		this.destroyMethod = destroyMethod;
	}

	public String getDestroyMethod() {
		return this.destroyMethod;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	@Override
	public Object getSource() {
		return this.source;
	}

}
