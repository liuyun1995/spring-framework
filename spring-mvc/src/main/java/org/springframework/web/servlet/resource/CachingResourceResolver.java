package org.springframework.web.servlet.resource;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

//缓存资源解析器
public class CachingResourceResolver extends AbstractResourceResolver {

	public static final String RESOLVED_RESOURCE_CACHE_KEY_PREFIX = "resolvedResource:";

	public static final String RESOLVED_URL_PATH_CACHE_KEY_PREFIX = "resolvedUrlPath:";

	private final Cache cache;

	//构造器
	public CachingResourceResolver(CacheManager cacheManager, String cacheName) {
		this(cacheManager.getCache(cacheName));
	}

	//构造器
	public CachingResourceResolver(Cache cache) {
		Assert.notNull(cache, "Cache is required");
		this.cache = cache;
	}

	//获取缓存
	public Cache getCache() {
		return this.cache;
	}

	@Override
	protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath,
			List<? extends Resource> locations, ResourceResolverChain chain) {
		String key = computeKey(request, requestPath);
		Resource resource = this.cache.get(key, Resource.class);
		if (resource != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Found match: " + resource);
			}
			return resource;
		}
		resource = chain.resolveResource(request, requestPath, locations);
		if (resource != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Putting resolved resource in cache: " + resource);
			}
			this.cache.put(key, resource);
		}
		return resource;
	}

	protected String computeKey(HttpServletRequest request, String requestPath) {
		StringBuilder key = new StringBuilder(RESOLVED_RESOURCE_CACHE_KEY_PREFIX);
		key.append(requestPath);
		if (request != null) {
			String encoding = request.getHeader("Accept-Encoding");
			if (encoding != null && encoding.contains("gzip")) {
				key.append("+encoding=gzip");
			}
		}
		return key.toString();
	}

	@Override
	protected String resolveUrlPathInternal(String resourceUrlPath,
			List<? extends Resource> locations, ResourceResolverChain chain) {

		String key = RESOLVED_URL_PATH_CACHE_KEY_PREFIX + resourceUrlPath;
		String resolvedUrlPath = this.cache.get(key, String.class);

		if (resolvedUrlPath != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Found match: \"" + resolvedUrlPath + "\"");
			}
			return resolvedUrlPath;
		}

		resolvedUrlPath = chain.resolveUrlPath(resourceUrlPath, locations);
		if (resolvedUrlPath != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Putting resolved resource URL path in cache: \"" + resolvedUrlPath + "\"");
			}
			this.cache.put(key, resolvedUrlPath);
		}

		return resolvedUrlPath;
	}

}
