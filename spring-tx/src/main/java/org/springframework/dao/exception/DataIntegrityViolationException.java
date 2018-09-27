package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class DataIntegrityViolationException extends NonTransientDataAccessException {

	public DataIntegrityViolationException(String msg) {
		super(msg);
	}

	public DataIntegrityViolationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
