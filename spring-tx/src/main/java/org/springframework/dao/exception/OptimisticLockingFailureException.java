package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class OptimisticLockingFailureException extends ConcurrencyFailureException {

	public OptimisticLockingFailureException(String msg) {
		super(msg);
	}

	public OptimisticLockingFailureException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
