package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class DuplicateKeyException extends DataIntegrityViolationException {

	public DuplicateKeyException(String msg) {
		super(msg);
	}

	public DuplicateKeyException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
