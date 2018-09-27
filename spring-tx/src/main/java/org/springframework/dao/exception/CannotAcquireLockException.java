package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class CannotAcquireLockException extends PessimisticLockingFailureException {

	public CannotAcquireLockException(String msg) {
		super(msg);
	}

	public CannotAcquireLockException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
