package org.springframework.web.servlet.mvc;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.CacheControl;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.springframework.web.util.UrlPathHelper;

//web内容拦截器
public class WebContentInterceptor extends WebContentGenerator implements HandlerInterceptor {

    private UrlPathHelper urlPathHelper = new UrlPathHelper();

    private PathMatcher pathMatcher = new AntPathMatcher();

    private Map<String, Integer> cacheMappings = new HashMap<String, Integer>();

    private Map<String, CacheControl> cacheControlMappings = new HashMap<String, CacheControl>();

    //构造器
    public WebContentInterceptor() {
        super(false);
    }

    //设置总是使用全路径
    public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
        this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
    }

    //设置是否对URL解码
    public void setUrlDecode(boolean urlDecode) {
        this.urlPathHelper.setUrlDecode(urlDecode);
    }

    //设置URL路径助手
    public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
        this.urlPathHelper = urlPathHelper;
    }

    //设置缓存映射
    public void setCacheMappings(Properties cacheMappings) {
        this.cacheMappings.clear();
        Enumeration<?> propNames = cacheMappings.propertyNames();
        while (propNames.hasMoreElements()) {
            String path = (String) propNames.nextElement();
            int cacheSeconds = Integer.valueOf(cacheMappings.getProperty(path));
            this.cacheMappings.put(path, cacheSeconds);
        }
    }

    //添加缓存映射
    public void addCacheMapping(CacheControl cacheControl, String... paths) {
        for (String path : paths) {
            this.cacheControlMappings.put(path, cacheControl);
        }
    }

    //设置路径匹配器
    public void setPathMatcher(PathMatcher pathMatcher) {
        Assert.notNull(pathMatcher, "PathMatcher must not be null");
        this.pathMatcher = pathMatcher;
    }

    //预处理
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws ServletException {

        checkRequest(request);

        String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
        if (logger.isDebugEnabled()) {
            logger.debug("Looking up cache seconds for [" + lookupPath + "]");
        }

        CacheControl cacheControl = lookupCacheControl(lookupPath);
        Integer cacheSeconds = lookupCacheSeconds(lookupPath);
        if (cacheControl != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Applying CacheControl to [" + lookupPath + "]");
            }
            applyCacheControl(response, cacheControl);
        } else if (cacheSeconds != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Applying CacheControl to [" + lookupPath + "]");
            }
            applyCacheSeconds(response, cacheSeconds);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Applying default cache seconds to [" + lookupPath + "]");
            }
            prepareResponse(response);
        }

        return true;
    }

    //寻找缓存控制器
    protected CacheControl lookupCacheControl(String urlPath) {
        // Direct match?
        CacheControl cacheControl = this.cacheControlMappings.get(urlPath);
        if (cacheControl != null) {
            return cacheControl;
        }
        // Pattern match?
        for (String registeredPath : this.cacheControlMappings.keySet()) {
            if (this.pathMatcher.match(registeredPath, urlPath)) {
                return this.cacheControlMappings.get(registeredPath);
            }
        }
        return null;
    }

    /**
     * Look up a cacheSeconds integer value for the given URL path.
     * <p>Supports direct matches, e.g. a registered "/test" matches "/test",
     * and various Ant-style pattern matches, e.g. a registered "/t*" matches
     * both "/test" and "/team". For details, see the AntPathMatcher class.
     *
     * @param urlPath URL the bean is mapped to
     * @return the cacheSeconds integer value, or {@code null} if not found
     * @see org.springframework.util.AntPathMatcher
     */
    protected Integer lookupCacheSeconds(String urlPath) {
        // Direct match?
        Integer cacheSeconds = this.cacheMappings.get(urlPath);
        if (cacheSeconds != null) {
            return cacheSeconds;
        }
        // Pattern match?
        for (String registeredPath : this.cacheMappings.keySet()) {
            if (this.pathMatcher.match(registeredPath, urlPath)) {
                return this.cacheMappings.get(registeredPath);
            }
        }
        return null;
    }


    /**
     * This implementation is empty.
     */
    @Override
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
    }

    /**
     * This implementation is empty.
     */
    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }

}
