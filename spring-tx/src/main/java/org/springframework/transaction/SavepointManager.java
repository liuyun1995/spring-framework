package org.springframework.transaction;

import org.springframework.transaction.exception.TransactionException;

//保存点管理器
public interface SavepointManager {

	//创建保存点
	Object createSavepoint() throws org.springframework.transaction.exception.TransactionException;

	//回滚到指定保存点
	void rollbackToSavepoint(Object savepoint) throws org.springframework.transaction.exception.TransactionException;

	//取消保存点
	void releaseSavepoint(Object savepoint) throws TransactionException;

}
