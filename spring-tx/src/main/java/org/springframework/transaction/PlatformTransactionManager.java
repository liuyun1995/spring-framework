package org.springframework.transaction;

import org.springframework.transaction.exception.TransactionException;

//平台事务管理器
public interface PlatformTransactionManager {

	//获取事务
	TransactionStatus getTransaction(TransactionDefinition definition) throws org.springframework.transaction.exception.TransactionException;

	//提交事务
	void commit(TransactionStatus status) throws org.springframework.transaction.exception.TransactionException;

	//回滚事务
	void rollback(TransactionStatus status) throws TransactionException;

}
