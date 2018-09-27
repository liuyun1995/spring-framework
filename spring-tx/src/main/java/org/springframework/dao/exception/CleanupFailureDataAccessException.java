package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class CleanupFailureDataAccessException extends NonTransientDataAccessException {

	public CleanupFailureDataAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
