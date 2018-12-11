package org.springframework.web.servlet.view;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

//抽象的缓存视图解析器
public abstract class AbstractCachingViewResolver extends WebApplicationObjectSupport implements ViewResolver {

    /**
     * Default maximum number of entries for the view cache: 1024
     */
    public static final int DEFAULT_CACHE_LIMIT = 1024;

    /**
     * Dummy marker object for unresolved views in the cache Maps
     */
    private static final View UNRESOLVED_VIEW = new View() {
        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
        }
    };


    /**
     * The maximum number of entries in the cache
     */
    private volatile int cacheLimit = DEFAULT_CACHE_LIMIT;

    /**
     * Whether we should refrain from resolving views again if unresolved once
     */
    private boolean cacheUnresolved = true;

    /**
     * Fast access cache for Views, returning already cached instances without a global lock
     */
    private final Map<Object, View> viewAccessCache = new ConcurrentHashMap<Object, View>(DEFAULT_CACHE_LIMIT);

    /**
     * Map from view key to View instance, synchronized for View creation
     */
    @SuppressWarnings("serial")
    private final Map<Object, View> viewCreationCache =
            new LinkedHashMap<Object, View>(DEFAULT_CACHE_LIMIT, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Object, View> eldest) {
                    if (size() > getCacheLimit()) {
                        viewAccessCache.remove(eldest.getKey());
                        return true;
                    } else {
                        return false;
                    }
                }
            };


    //设置最大缓存视图数
    public void setCacheLimit(int cacheLimit) {
        this.cacheLimit = cacheLimit;
    }

    //获取最大缓存视图数
    public int getCacheLimit() {
        return this.cacheLimit;
    }

    //设置是否缓存
    public void setCache(boolean cache) {
        this.cacheLimit = (cache ? DEFAULT_CACHE_LIMIT : 0);
    }

    //是否设置缓存
    public boolean isCache() {
        return (this.cacheLimit > 0);
    }

    /**
     * Whether a view name once resolved to {@code null} should be cached and
     * automatically resolved to {@code null} subsequently.
     * <p>Default is "true": unresolved view names are being cached, as of Spring 3.1.
     * Note that this flag only applies if the general {@link #setCache "cache"}
     * flag is kept at its default of "true" as well.
     * <p>Of specific interest is the ability for some AbstractUrlBasedView
     * implementations (FreeMarker, Velocity, Tiles) to check if an underlying
     * resource exists via {@link AbstractUrlBasedView#checkResource(Locale)}.
     * With this flag set to "false", an underlying resource that re-appears
     * is noticed and used. With the flag set to "true", one check is made only.
     */
    public void setCacheUnresolved(boolean cacheUnresolved) {
        this.cacheUnresolved = cacheUnresolved;
    }

    /**
     * Return if caching of unresolved views is enabled.
     */
    public boolean isCacheUnresolved() {
        return this.cacheUnresolved;
    }


    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        //若未设置缓存，则直接创建视图
        if (!isCache()) {
            return createView(viewName, locale);
        } else {
            Object cacheKey = getCacheKey(viewName, locale);
            View view = this.viewAccessCache.get(cacheKey);
            if (view == null) {
                synchronized (this.viewCreationCache) {
                    view = this.viewCreationCache.get(cacheKey);
                    if (view == null) {
                        // Ask the subclass to create the View object.
                        view = createView(viewName, locale);
                        if (view == null && this.cacheUnresolved) {
                            view = UNRESOLVED_VIEW;
                        }
                        if (view != null) {
                            this.viewAccessCache.put(cacheKey, view);
                            this.viewCreationCache.put(cacheKey, view);
                            if (logger.isTraceEnabled()) {
                                logger.trace("Cached view [" + cacheKey + "]");
                            }
                        }
                    }
                }
            }
            return (view != UNRESOLVED_VIEW ? view : null);
        }
    }

    //获取缓存键
    protected Object getCacheKey(String viewName, Locale locale) {
        return viewName + '_' + locale;
    }

    //从缓存中移除视图
    public void removeFromCache(String viewName, Locale locale) {
        if (!isCache()) {
            logger.warn("View caching is SWITCHED OFF -- removal not necessary");
        } else {
            Object cacheKey = getCacheKey(viewName, locale);
            Object cachedView;
            synchronized (this.viewCreationCache) {
                this.viewAccessCache.remove(cacheKey);
                cachedView = this.viewCreationCache.remove(cacheKey);
            }
            if (logger.isDebugEnabled()) {
                // Some debug output might be useful...
                if (cachedView == null) {
                    logger.debug("No cached instance for view '" + cacheKey + "' was found");
                } else {
                    logger.debug("Cache for view " + cacheKey + " has been cleared");
                }
            }
        }
    }

    //清空缓存
    public void clearCache() {
        logger.debug("Clearing entire view cache");
        synchronized (this.viewCreationCache) {
            this.viewAccessCache.clear();
            this.viewCreationCache.clear();
        }
    }

    //创建视图对象
    protected View createView(String viewName, Locale locale) throws Exception {
        return loadView(viewName, locale);
    }

    //加载视图对象
    protected abstract View loadView(String viewName, Locale locale) throws Exception;

}
