package org.springframework.beans.factory.wiring;

import org.springframework.beans.factory.AutowireCapableBeanFactory;
import org.springframework.util.Assert;

public class BeanWiringInfo {

	public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;
	public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;
	private String beanName = null;
	private boolean isDefaultBeanName = false;
	private int autowireMode = AutowireCapableBeanFactory.AUTOWIRE_NO;
	private boolean dependencyCheck = false;

	//构造器1
	public BeanWiringInfo() {}

	//构造器2
	public BeanWiringInfo(String beanName) {
		this(beanName, false);
	}

	//构造器3
	public BeanWiringInfo(String beanName, boolean isDefaultBeanName) {
		Assert.hasText(beanName, "'beanName' must not be empty");
		this.beanName = beanName;
		this.isDefaultBeanName = isDefaultBeanName;
	}

	//构造器4
	public BeanWiringInfo(int autowireMode, boolean dependencyCheck) {
		if (autowireMode != AUTOWIRE_BY_NAME && autowireMode != AUTOWIRE_BY_TYPE) {
			throw new IllegalArgumentException("Only constants AUTOWIRE_BY_NAME and AUTOWIRE_BY_TYPE supported");
		}
		this.autowireMode = autowireMode;
		this.dependencyCheck = dependencyCheck;
	}

	//是否自动写入
	public boolean indicatesAutowiring() {
		return (this.beanName == null);
	}

	//获取Bean名称
	public String getBeanName() {
		return this.beanName;
	}

	//是否默认Bean名称
	public boolean isDefaultBeanName() {
		return this.isDefaultBeanName;
	}

	//获取自动装配模式
	public int getAutowireMode() {
		return this.autowireMode;
	}

	//获取是否要依赖检查
	public boolean getDependencyCheck() {
		return this.dependencyCheck;
	}

}
