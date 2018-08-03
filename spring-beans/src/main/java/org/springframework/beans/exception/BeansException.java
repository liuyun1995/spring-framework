package org.springframework.beans.exception;

import org.springframework.core.NestedRuntimeException;
import org.springframework.util.ObjectUtils;

//抽象的Bean异常
public abstract class BeansException extends NestedRuntimeException {
	
	public BeansException(String msg) {
		super(msg);
	}
	
	public BeansException(String msg, Throwable cause) {
		super(msg, cause);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansException)) {
			return false;
		}
		BeansException otherBe = (BeansException) other;
		return (getMessage().equals(otherBe.getMessage()) &&
				ObjectUtils.nullSafeEquals(getCause(), otherBe.getCause()));
	}

	@Override
	public int hashCode() {
		return getMessage().hashCode();
	}

}
