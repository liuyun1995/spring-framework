package org.springframework.web.servlet.resource;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

//缓存资源转换器
public class CachingResourceTransformer implements ResourceTransformer {

	private static final Log logger = LogFactory.getLog(CachingResourceTransformer.class);

	private final Cache cache;

	//构造器
	public CachingResourceTransformer(CacheManager cacheManager, String cacheName) {
		this(cacheManager.getCache(cacheName));
	}

	//构造器
	public CachingResourceTransformer(Cache cache) {
		Assert.notNull(cache, "Cache is required");
		this.cache = cache;
	}

	//获取缓存
	public Cache getCache() {
		return this.cache;
	}

	@Override
	public Resource transform(HttpServletRequest request, Resource resource, ResourceTransformerChain transformerChain)
			throws IOException {
		Resource transformed = this.cache.get(resource, Resource.class);
		if (transformed != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Found match: " + transformed);
			}
			return transformed;
		}
		transformed = transformerChain.transform(request, resource);
		if (logger.isTraceEnabled()) {
			logger.trace("Putting transformed resource in cache: " + transformed);
		}
		this.cache.put(resource, transformed);
		return transformed;
	}

}
