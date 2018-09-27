package org.springframework.dao.exception;

@SuppressWarnings("serial")
public abstract class NonTransientDataAccessException extends DataAccessException {

	public NonTransientDataAccessException(String msg) {
		super(msg);
	}

	public NonTransientDataAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
