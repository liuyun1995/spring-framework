package org.springframework.beans.bean.definition;

import org.springframework.util.StringUtils;

//默认Bean定义
public class BeanDefinitionDefaults {

	private boolean lazyInit;                                                     //是否懒加载
	private int dependencyCheck = AbstractBeanDefinition.DEPENDENCY_CHECK_NONE;   //依赖检查
	private int autowireMode = AbstractBeanDefinition.AUTOWIRE_NO;                //自动装配模式
	private String initMethodName;                                                //初始化方法
	private String destroyMethodName;                                             //销毁方法

	//设置是否懒加载
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	//是否懒加载
	public boolean isLazyInit() {
		return this.lazyInit;
	}

	//设置依赖检查模式
	public void setDependencyCheck(int dependencyCheck) {
		this.dependencyCheck = dependencyCheck;
	}

	//获取依赖检查模式
	public int getDependencyCheck() {
		return this.dependencyCheck;
	}

	//设置自动装配模式
	public void setAutowireMode(int autowireMode) {
		this.autowireMode = autowireMode;
	}

	//获取自动装配模式
	public int getAutowireMode() {
		return this.autowireMode;
	}

	//设置初始化方法名
	public void setInitMethodName(String initMethodName) {
		this.initMethodName = (StringUtils.hasText(initMethodName) ? initMethodName : null);
	}

	//获取初始化方法名
	public String getInitMethodName() {
		return this.initMethodName;
	}

	//设置销毁方法名
	public void setDestroyMethodName(String destroyMethodName) {
		this.destroyMethodName = (StringUtils.hasText(destroyMethodName) ? destroyMethodName : null);
	}

	//获取销毁方法名
	public String getDestroyMethodName() {
		return this.destroyMethodName;
	}

}
