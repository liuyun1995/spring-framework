package org.springframework.transaction.exception;

import org.springframework.transaction.exception.TransactionException;

@SuppressWarnings("serial")
public class TransactionTimedOutException extends TransactionException {

	public TransactionTimedOutException(String msg) {
		super(msg);
	}

	public TransactionTimedOutException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
