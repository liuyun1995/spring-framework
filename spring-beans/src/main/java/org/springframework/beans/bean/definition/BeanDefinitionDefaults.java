package org.springframework.beans.bean.definition;

import org.springframework.util.StringUtils;

//默认Bean定义
public class BeanDefinitionDefaults {

	private boolean lazyInit;                                                     //是否懒加载
	private int dependencyCheck = AbstractBeanDefinition.DEPENDENCY_CHECK_NONE;   //依赖检查
	private int autowireMode = AbstractBeanDefinition.AUTOWIRE_NO;                //自动装配模式
	private String initMethodName;                                                //初始化方法
	private String destroyMethodName;                                             //销毁方法

	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	public boolean isLazyInit() {
		return this.lazyInit;
	}

	public void setDependencyCheck(int dependencyCheck) {
		this.dependencyCheck = dependencyCheck;
	}

	public int getDependencyCheck() {
		return this.dependencyCheck;
	}

	public void setAutowireMode(int autowireMode) {
		this.autowireMode = autowireMode;
	}

	public int getAutowireMode() {
		return this.autowireMode;
	}

	public void setInitMethodName(String initMethodName) {
		this.initMethodName = (StringUtils.hasText(initMethodName) ? initMethodName : null);
	}

	public String getInitMethodName() {
		return this.initMethodName;
	}

	public void setDestroyMethodName(String destroyMethodName) {
		this.destroyMethodName = (StringUtils.hasText(destroyMethodName) ? destroyMethodName : null);
	}

	public String getDestroyMethodName() {
		return this.destroyMethodName;
	}

}
