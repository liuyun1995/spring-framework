package org.springframework.beans;

//无效属性异常
@SuppressWarnings("serial")
public class InvalidPropertyException extends FatalBeanException {

	private Class<?> beanClass;

	private String propertyName;
	
	public InvalidPropertyException(Class<?> beanClass, String propertyName, String msg) {
		this(beanClass, propertyName, msg, null);
	}
	
	public InvalidPropertyException(Class<?> beanClass, String propertyName, String msg, Throwable cause) {
		super("Invalid property '" + propertyName + "' of bean class [" + beanClass.getName() + "]: " + msg, cause);
		this.beanClass = beanClass;
		this.propertyName = propertyName;
	}
	
	public Class<?> getBeanClass() {
		return beanClass;
	}
	
	public String getPropertyName() {
		return propertyName;
	}

}
