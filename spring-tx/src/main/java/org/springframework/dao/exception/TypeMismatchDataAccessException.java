package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class TypeMismatchDataAccessException extends InvalidDataAccessResourceUsageException {

	public TypeMismatchDataAccessException(String msg) {
		super(msg);
	}

	public TypeMismatchDataAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
