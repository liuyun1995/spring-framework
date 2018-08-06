package org.springframework.beans.annotation;

import org.springframework.beans.factory.AutowireCapableBeanFactory;

//自动装配枚举
public enum Autowire {

	NO(AutowireCapableBeanFactory.AUTOWIRE_NO),
	BY_NAME(AutowireCapableBeanFactory.AUTOWIRE_BY_NAME),
	BY_TYPE(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);

	private final int value;

	Autowire(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}
	
	public boolean isAutowire() {
		return (this == BY_NAME || this == BY_TYPE);
	}

}
