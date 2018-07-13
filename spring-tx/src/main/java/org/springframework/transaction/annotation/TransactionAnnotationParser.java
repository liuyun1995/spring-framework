package org.springframework.transaction.annotation;

import java.lang.reflect.AnnotatedElement;

import org.springframework.transaction.interceptor.TransactionAttribute;

//事务注解解析器
public interface TransactionAnnotationParser {
	
	TransactionAttribute parseTransactionAnnotation(AnnotatedElement ae);

}
