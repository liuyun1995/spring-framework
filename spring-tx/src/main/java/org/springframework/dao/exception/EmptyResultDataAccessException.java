package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException {

	public EmptyResultDataAccessException(int expectedSize) {
		super(expectedSize, 0);
	}

	public EmptyResultDataAccessException(String msg, int expectedSize) {
		super(msg, expectedSize, 0);
	}

	public EmptyResultDataAccessException(String msg, int expectedSize, Throwable ex) {
		super(msg, expectedSize, 0, ex);
	}

}
