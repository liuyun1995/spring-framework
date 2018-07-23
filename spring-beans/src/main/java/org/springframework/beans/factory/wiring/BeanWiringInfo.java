package org.springframework.beans.factory.wiring;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.util.Assert;

public class BeanWiringInfo {

	/**
	 * Constant that indicates autowiring bean properties by name.
	 * @see #BeanWiringInfo(int, boolean)
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#AUTOWIRE_BY_NAME
	 */
	public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

	/**
	 * Constant that indicates autowiring bean properties by type.
	 * @see #BeanWiringInfo(int, boolean)
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#AUTOWIRE_BY_TYPE
	 */
	public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;


	private String beanName = null;

	private boolean isDefaultBeanName = false;

	private int autowireMode = AutowireCapableBeanFactory.AUTOWIRE_NO;

	private boolean dependencyCheck = false;


	public BeanWiringInfo() {}

	public BeanWiringInfo(String beanName) {
		this(beanName, false);
	}

	public BeanWiringInfo(String beanName, boolean isDefaultBeanName) {
		Assert.hasText(beanName, "'beanName' must not be empty");
		this.beanName = beanName;
		this.isDefaultBeanName = isDefaultBeanName;
	}

	public BeanWiringInfo(int autowireMode, boolean dependencyCheck) {
		if (autowireMode != AUTOWIRE_BY_NAME && autowireMode != AUTOWIRE_BY_TYPE) {
			throw new IllegalArgumentException("Only constants AUTOWIRE_BY_NAME and AUTOWIRE_BY_TYPE supported");
		}
		this.autowireMode = autowireMode;
		this.dependencyCheck = dependencyCheck;
	}

	public boolean indicatesAutowiring() {
		return (this.beanName == null);
	}


	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * Return whether the specific bean name is a suggested default bean name,
	 * not necessarily matching an actual bean definition in the factory.
	 */
	public boolean isDefaultBeanName() {
		return this.isDefaultBeanName;
	}

	/**
	 * Return one of the constants {@link #AUTOWIRE_BY_NAME} /
	 * {@link #AUTOWIRE_BY_TYPE}, if autowiring is indicated.
	 */
	public int getAutowireMode() {
		return this.autowireMode;
	}

	/**
	 * Return whether to perform a dependency check for object references
	 * in the bean instance (after autowiring).
	 */
	public boolean getDependencyCheck() {
		return this.dependencyCheck;
	}

}
