package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class DataRetrievalFailureException extends NonTransientDataAccessException {

	public DataRetrievalFailureException(String msg) {
		super(msg);
	}

	public DataRetrievalFailureException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
