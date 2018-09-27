package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;

//事务属性源
public interface TransactionAttributeSource {

	//获取事务属性
	TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass);

}
