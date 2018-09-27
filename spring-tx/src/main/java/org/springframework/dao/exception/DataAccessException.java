package org.springframework.dao.exception;

import org.springframework.core.NestedRuntimeException;

@SuppressWarnings("serial")
public abstract class DataAccessException extends NestedRuntimeException {

	public DataAccessException(String msg) {
		super(msg);
	}

	public DataAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
