package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class CannotSerializeTransactionException extends PessimisticLockingFailureException {

	public CannotSerializeTransactionException(String msg) {
		super(msg);
	}

	public CannotSerializeTransactionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
