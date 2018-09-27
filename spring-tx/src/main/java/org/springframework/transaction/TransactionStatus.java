package org.springframework.transaction;

import java.io.Flushable;

//事务状态
public interface TransactionStatus extends SavepointManager, Flushable {

	//是否是新事务
	boolean isNewTransaction();

	//是否有保存点
	boolean hasSavepoint();

	//设置仅回滚
	void setRollbackOnly();

	//是否仅回滚
	boolean isRollbackOnly();

	/**
	 * Flush the underlying session to the datastore, if applicable:
	 * for example, all affected Hibernate/JPA sessions.
	 * <p>This is effectively just a hint and may be a no-op if the underlying
	 * transaction manager does not have a flush concept. A flush signal may
	 * get applied to the primary resource or to transaction synchronizations,
	 * depending on the underlying resource.
	 */
	@Override
	void flush();

	//是否已完成
	boolean isCompleted();

}
