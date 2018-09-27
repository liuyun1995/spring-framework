package org.springframework.transaction.exception;

import org.springframework.core.NestedRuntimeException;

//事务异常
@SuppressWarnings("serial")
public abstract class TransactionException extends NestedRuntimeException {
	
	public TransactionException(String msg) {
		super(msg);
	}
	
	public TransactionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
