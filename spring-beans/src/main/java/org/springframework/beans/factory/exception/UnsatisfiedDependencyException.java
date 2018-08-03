package org.springframework.beans.factory.exception;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

@SuppressWarnings("serial")
public class UnsatisfiedDependencyException extends BeanCreationException {

	private InjectionPoint injectionPoint;

	public UnsatisfiedDependencyException(
			String resourceDescription, String beanName, String propertyName, String msg) {
		super(resourceDescription, beanName,
				"Unsatisfied dependency expressed through bean property '" + propertyName + "'" +
				(StringUtils.hasLength(msg) ? ": " + msg : ""));
	}

	public UnsatisfiedDependencyException(
			String resourceDescription, String beanName, String propertyName, BeansException ex) {
		this(resourceDescription, beanName, propertyName, "");
		initCause(ex);
	}

	public UnsatisfiedDependencyException(
			String resourceDescription, String beanName, InjectionPoint injectionPoint, String msg) {
		super(resourceDescription, beanName,
				"Unsatisfied dependency expressed through " + injectionPoint +
				(StringUtils.hasLength(msg) ? ": " + msg : ""));
		this.injectionPoint = injectionPoint;
	}

	public UnsatisfiedDependencyException(
			String resourceDescription, String beanName, InjectionPoint injectionPoint, BeansException ex) {
		this(resourceDescription, beanName, injectionPoint, "");
		initCause(ex);
	}

	@Deprecated
	public UnsatisfiedDependencyException(
			String resourceDescription, String beanName, int ctorArgIndex, Class<?> ctorArgType, String msg) {
		super(resourceDescription, beanName,
				"Unsatisfied dependency expressed through constructor argument with index " +
				ctorArgIndex + " of type [" + ClassUtils.getQualifiedName(ctorArgType) + "]" +
				(msg != null ? ": " + msg : ""));
	}

	@Deprecated
	public UnsatisfiedDependencyException(
			String resourceDescription, String beanName, int ctorArgIndex, Class<?> ctorArgType, BeansException ex) {
		this(resourceDescription, beanName, ctorArgIndex, ctorArgType, (ex != null ? ex.getMessage() : ""));
		initCause(ex);
	}

	public InjectionPoint getInjectionPoint() {
		return this.injectionPoint;
	}

}
