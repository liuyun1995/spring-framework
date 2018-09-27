package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class NonTransientDataAccessResourceException extends NonTransientDataAccessException {

	public NonTransientDataAccessResourceException(String msg) {
		super(msg);
	}

	public NonTransientDataAccessResourceException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
