package org.springframework.transaction.exception;

import org.springframework.transaction.exception.TransactionException;

@SuppressWarnings("serial")
public class TransactionUsageException extends TransactionException {

	public TransactionUsageException(String msg) {
		super(msg);
	}

	public TransactionUsageException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
