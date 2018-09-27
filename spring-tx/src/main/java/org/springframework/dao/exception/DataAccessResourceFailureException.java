package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class DataAccessResourceFailureException extends NonTransientDataAccessResourceException {

	public DataAccessResourceFailureException(String msg) {
		super(msg);
	}

	public DataAccessResourceFailureException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
