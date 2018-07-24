package org.springframework.cache.annotation;

import org.springframework.cache.interceptor.CacheOperation;

import java.lang.reflect.Method;
import java.util.Collection;

//缓存注解解析器
public interface CacheAnnotationParser {

	//按类型解析缓存注解
	Collection<CacheOperation> parseCacheAnnotations(Class<?> type);

	//按方法解析缓存注解
	Collection<CacheOperation> parseCacheAnnotations(Method method);

}
