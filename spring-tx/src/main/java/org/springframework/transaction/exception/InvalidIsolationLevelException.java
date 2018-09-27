package org.springframework.transaction.exception;

@SuppressWarnings("serial")
public class InvalidIsolationLevelException extends TransactionUsageException {

	public InvalidIsolationLevelException(String msg) {
		super(msg);
	}

}
