package org.springframework.dao.exception;

@SuppressWarnings("serial")
public class DeadlockLoserDataAccessException extends PessimisticLockingFailureException {

	public DeadlockLoserDataAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
