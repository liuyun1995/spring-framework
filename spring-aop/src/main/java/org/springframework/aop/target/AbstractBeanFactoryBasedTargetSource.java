package org.springframework.aop.target;

import java.io.Serializable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.ObjectUtils;

//基于目标源的抽象Bean工厂
public abstract class AbstractBeanFactoryBasedTargetSource implements TargetSource, BeanFactoryAware, Serializable {

	private static final long serialVersionUID = -4721607536018568393L;

	protected final Log logger = LogFactory.getLog(getClass());

	private String targetBeanName;     //目标Bean名称
	private Class<?> targetClass;      //目标类型
	private BeanFactory beanFactory;   //Bean工厂

	//设置目标Bean名称
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	//获取目标Bean名称
	public String getTargetBeanName() {
		return this.targetBeanName;
	}

	//设置目标类型
	public void setTargetClass(Class<?> targetClass) {
		this.targetClass = targetClass;
	}

	//设置Bean工厂
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (this.targetBeanName == null) {
			throw new IllegalStateException("Property 'targetBeanName' is required");
		}
		this.beanFactory = beanFactory;
	}

	//获取Bean工厂
	public BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	//获取目标类型
	@Override
	public synchronized Class<?> getTargetClass() {
		if (this.targetClass == null && this.beanFactory != null) {
			// Determine type of the target bean.
			this.targetClass = this.beanFactory.getType(this.targetBeanName);
			if (this.targetClass == null) {
				if (logger.isTraceEnabled()) {
					logger.trace("Getting bean with name '" + this.targetBeanName + "' in order to determine type");
				}
				Object beanInstance = this.beanFactory.getBean(this.targetBeanName);
				if (beanInstance != null) {
					this.targetClass = beanInstance.getClass();
				}
			}
		}
		return this.targetClass;
	}

	//是否是静态的
	@Override
	public boolean isStatic() {
		return false;
	}

	//释放目标对象
	@Override
	public void releaseTarget(Object target) throws Exception {
		// Nothing to do here.
	}

	//拷贝方法
	protected void copyFrom(AbstractBeanFactoryBasedTargetSource other) {
		this.targetBeanName = other.targetBeanName;
		this.targetClass = other.targetClass;
		this.beanFactory = other.beanFactory;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		AbstractBeanFactoryBasedTargetSource otherTargetSource = (AbstractBeanFactoryBasedTargetSource) other;
		return (ObjectUtils.nullSafeEquals(this.beanFactory, otherTargetSource.beanFactory) &&
				ObjectUtils.nullSafeEquals(this.targetBeanName, otherTargetSource.targetBeanName));
	}

	@Override
	public int hashCode() {
		int hashCode = getClass().hashCode();
		hashCode = 13 * hashCode + ObjectUtils.nullSafeHashCode(this.beanFactory);
		hashCode = 13 * hashCode + ObjectUtils.nullSafeHashCode(this.targetBeanName);
		return hashCode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append(" for target bean '").append(this.targetBeanName).append("'");
		if (this.targetClass != null) {
			sb.append(" of type [").append(this.targetClass.getName()).append("]");
		}
		return sb.toString();
	}

}
