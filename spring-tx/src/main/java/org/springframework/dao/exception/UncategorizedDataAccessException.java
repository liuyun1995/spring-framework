package org.springframework.dao.exception;

@SuppressWarnings("serial")
public abstract class UncategorizedDataAccessException extends NonTransientDataAccessException {

	public UncategorizedDataAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
