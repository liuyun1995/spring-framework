package org.springframework.transaction.exception;

import org.springframework.transaction.exception.CannotCreateTransactionException;

@SuppressWarnings("serial")
public class TransactionSuspensionNotSupportedException extends CannotCreateTransactionException {

	public TransactionSuspensionNotSupportedException(String msg) {
		super(msg);
	}

	public TransactionSuspensionNotSupportedException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
