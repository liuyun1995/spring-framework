package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class PessimisticLockingFailureException extends ConcurrencyFailureException {

	public PessimisticLockingFailureException(String msg) {
		super(msg);
	}

	public PessimisticLockingFailureException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
