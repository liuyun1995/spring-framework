package org.springframework.transaction.exception;

//不能建立事务异常
@SuppressWarnings("serial")
public class CannotCreateTransactionException extends TransactionException {
	
	public CannotCreateTransactionException(String msg) {
		super(msg);
	}
	
	public CannotCreateTransactionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
