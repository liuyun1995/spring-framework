package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class QueryTimeoutException extends TransientDataAccessException {

	public QueryTimeoutException(String msg) {
		super(msg);
	}

	public QueryTimeoutException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
