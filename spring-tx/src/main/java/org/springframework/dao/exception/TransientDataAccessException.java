package org.springframework.dao.exception;

@SuppressWarnings("serial")
public abstract class TransientDataAccessException extends DataAccessException {

	public TransientDataAccessException(String msg) {
		super(msg);
	}

	public TransientDataAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
