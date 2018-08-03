package org.springframework.beans.factory.bean.definition;

import org.springframework.beans.exception.FatalBeanException;

//Bean定义验证异常
@SuppressWarnings("serial")
public class BeanDefinitionValidationException extends FatalBeanException {
	
	public BeanDefinitionValidationException(String msg) {
		super(msg);
	}
	
	public BeanDefinitionValidationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
