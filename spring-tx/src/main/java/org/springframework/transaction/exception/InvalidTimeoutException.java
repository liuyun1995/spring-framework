package org.springframework.transaction.exception;

@SuppressWarnings("serial")
public class InvalidTimeoutException extends TransactionUsageException {

	private int timeout;

	public InvalidTimeoutException(String msg, int timeout) {
		super(msg);
		this.timeout = timeout;
	}

	public int getTimeout() {
		return timeout;
	}

}
