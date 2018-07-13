package org.springframework.cache;

import java.util.Collection;

//缓存管理器
public interface CacheManager {

	//根据名称获取缓存
	Cache getCache(String name);

	//获取缓存名集合
	Collection<String> getCacheNames();

}
