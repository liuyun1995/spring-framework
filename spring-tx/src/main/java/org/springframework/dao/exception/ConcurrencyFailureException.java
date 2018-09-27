package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class ConcurrencyFailureException extends TransientDataAccessException {

	public ConcurrencyFailureException(String msg) {
		super(msg);
	}

	public ConcurrencyFailureException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
