package org.springframework.aop.target;

//池化配置
public interface PoolingConfig {

	//获取最大大小
	int getMaxSize();

	/**
	 * Return the number of active objects in the pool.
	 * @throws UnsupportedOperationException if not supported by the pool
	 */
	int getActiveCount() throws UnsupportedOperationException;

	/**
	 * Return the number of idle objects in the pool.
	 * @throws UnsupportedOperationException if not supported by the pool
	 */
	int getIdleCount() throws UnsupportedOperationException;

}
