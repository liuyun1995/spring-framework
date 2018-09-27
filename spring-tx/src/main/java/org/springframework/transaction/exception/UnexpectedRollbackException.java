package org.springframework.transaction.exception;

import org.springframework.transaction.exception.TransactionException;

@SuppressWarnings("serial")
public class UnexpectedRollbackException extends TransactionException {

	public UnexpectedRollbackException(String msg) {
		super(msg);
	}

	public UnexpectedRollbackException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
