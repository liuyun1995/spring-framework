package org.springframework.web.servlet.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;

//抽象处理器映射器
public abstract class AbstractHandlerMapping extends WebApplicationObjectSupport implements HandlerMapping, Ordered {

    private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

    private Object defaultHandler;

    private UrlPathHelper urlPathHelper = new UrlPathHelper();

    private PathMatcher pathMatcher = new AntPathMatcher();

    private final List<Object> interceptors = new ArrayList<Object>();

    private final List<HandlerInterceptor> adaptedInterceptors = new ArrayList<HandlerInterceptor>();

    private final UrlBasedCorsConfigurationSource globalCorsConfigSource = new UrlBasedCorsConfigurationSource();

    private CorsProcessor corsProcessor = new DefaultCorsProcessor();


    //设置序号
    public final void setOrder(int order) {
        this.order = order;
    }

    //获取序号
    @Override
    public final int getOrder() {
        return this.order;
    }

    //设置默认处理器
    public void setDefaultHandler(Object defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    //获取默认处理器
    public Object getDefaultHandler() {
        return this.defaultHandler;
    }

    //设置总是使用全路径
    public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
        this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
        this.globalCorsConfigSource.setAlwaysUseFullPath(alwaysUseFullPath);
    }

    /**
     * Set if context path and request URI should be URL-decoded. Both are returned
     * <i>undecoded</i> by the Servlet API, in contrast to the servlet path.
     * <p>Uses either the request encoding or the default encoding according
     * to the Servlet spec (ISO-8859-1).
     *
     * @see org.springframework.web.util.UrlPathHelper#setUrlDecode
     */
    public void setUrlDecode(boolean urlDecode) {
        this.urlPathHelper.setUrlDecode(urlDecode);
        this.globalCorsConfigSource.setUrlDecode(urlDecode);
    }

    /**
     * Set if ";" (semicolon) content should be stripped from the request URI.
     * <p>The default value is {@code true}.
     *
     * @see org.springframework.web.util.UrlPathHelper#setRemoveSemicolonContent(boolean)
     */
    public void setRemoveSemicolonContent(boolean removeSemicolonContent) {
        this.urlPathHelper.setRemoveSemicolonContent(removeSemicolonContent);
        this.globalCorsConfigSource.setRemoveSemicolonContent(removeSemicolonContent);
    }

    /**
     * Set the UrlPathHelper to use for resolution of lookup paths.
     * <p>Use this to override the default UrlPathHelper with a custom subclass,
     * or to share common UrlPathHelper settings across multiple HandlerMappings
     * and MethodNameResolvers.
     */
    public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
        this.urlPathHelper = urlPathHelper;
        this.globalCorsConfigSource.setUrlPathHelper(urlPathHelper);
    }

    /**
     * Return the UrlPathHelper implementation to use for resolution of lookup paths.
     */
    public UrlPathHelper getUrlPathHelper() {
        return urlPathHelper;
    }

    /**
     * Set the PathMatcher implementation to use for matching URL paths
     * against registered URL patterns. Default is AntPathMatcher.
     *
     * @see org.springframework.util.AntPathMatcher
     */
    public void setPathMatcher(PathMatcher pathMatcher) {
        Assert.notNull(pathMatcher, "PathMatcher must not be null");
        this.pathMatcher = pathMatcher;
        this.globalCorsConfigSource.setPathMatcher(pathMatcher);
    }

    /**
     * Return the PathMatcher implementation to use for matching URL paths
     * against registered URL patterns.
     */
    public PathMatcher getPathMatcher() {
        return this.pathMatcher;
    }

    /**
     * Set the interceptors to apply for all handlers mapped by this handler mapping.
     * <p>Supported interceptor types are HandlerInterceptor, WebRequestInterceptor, and MappedInterceptor.
     * Mapped interceptors apply only to request URLs that match its path patterns.
     * Mapped interceptor beans are also detected by type during initialization.
     *
     * @param interceptors array of handler interceptors
     * @see #adaptInterceptor
     * @see org.springframework.web.servlet.HandlerInterceptor
     * @see org.springframework.web.context.request.WebRequestInterceptor
     */
    public void setInterceptors(Object... interceptors) {
        this.interceptors.addAll(Arrays.asList(interceptors));
    }

    /**
     * Set "global" CORS configuration based on URL patterns. By default the first
     * matching URL pattern is combined with the CORS configuration for the
     * handler, if any.
     *
     * @since 4.2
     */
    public void setCorsConfigurations(Map<String, CorsConfiguration> corsConfigurations) {
        this.globalCorsConfigSource.setCorsConfigurations(corsConfigurations);
    }

    /**
     * Get the "global" CORS configuration.
     */
    public Map<String, CorsConfiguration> getCorsConfigurations() {
        return this.globalCorsConfigSource.getCorsConfigurations();
    }

    /**
     * Configure a custom {@link CorsProcessor} to use to apply the matched
     * {@link CorsConfiguration} for a request.
     * <p>By default {@link DefaultCorsProcessor} is used.
     *
     * @since 4.2
     */
    public void setCorsProcessor(CorsProcessor corsProcessor) {
        Assert.notNull(corsProcessor, "CorsProcessor must not be null");
        this.corsProcessor = corsProcessor;
    }

    /**
     * Return the configured {@link CorsProcessor}.
     */
    public CorsProcessor getCorsProcessor() {
        return this.corsProcessor;
    }


    /**
     * Initializes the interceptors.
     *
     * @see #extendInterceptors(java.util.List)
     * @see #initInterceptors()
     */
    @Override
    protected void initApplicationContext() throws BeansException {
        extendInterceptors(this.interceptors);
        detectMappedInterceptors(this.adaptedInterceptors);
        initInterceptors();
    }

    /**
     * Extension hook that subclasses can override to register additional interceptors,
     * given the configured interceptors (see {@link #setInterceptors}).
     * <p>Will be invoked before {@link #initInterceptors()} adapts the specified
     * interceptors into {@link HandlerInterceptor} instances.
     * <p>The default implementation is empty.
     *
     * @param interceptors the configured interceptor List (never {@code null}), allowing
     *                     to add further interceptors before as well as after the existing interceptors
     */
    protected void extendInterceptors(List<Object> interceptors) {
    }

    /**
     * Detect beans of type {@link MappedInterceptor} and add them to the list of mapped interceptors.
     * <p>This is called in addition to any {@link MappedInterceptor}s that may have been provided
     * via {@link #setInterceptors}, by default adding all beans of type {@link MappedInterceptor}
     * from the current context and its ancestors. Subclasses can override and refine this policy.
     *
     * @param mappedInterceptors an empty list to add {@link MappedInterceptor} instances to
     */
    protected void detectMappedInterceptors(List<HandlerInterceptor> mappedInterceptors) {
        mappedInterceptors.addAll(
                BeanFactoryUtils.beansOfTypeIncludingAncestors(
                        getApplicationContext(), MappedInterceptor.class, true, false).values());
    }

    /**
     * Initialize the specified interceptors, checking for {@link MappedInterceptor}s and
     * adapting {@link HandlerInterceptor}s and {@link WebRequestInterceptor}s if necessary.
     *
     * @see #setInterceptors
     * @see #adaptInterceptor
     */
    protected void initInterceptors() {
        if (!this.interceptors.isEmpty()) {
            for (int i = 0; i < this.interceptors.size(); i++) {
                Object interceptor = this.interceptors.get(i);
                if (interceptor == null) {
                    throw new IllegalArgumentException("Entry number " + i + " in interceptors array is null");
                }
                this.adaptedInterceptors.add(adaptInterceptor(interceptor));
            }
        }
    }

    /**
     * Adapt the given interceptor object to the {@link HandlerInterceptor} interface.
     * <p>By default, the supported interceptor types are {@link HandlerInterceptor}
     * and {@link WebRequestInterceptor}. Each given {@link WebRequestInterceptor}
     * will be wrapped in a {@link WebRequestHandlerInterceptorAdapter}.
     * Can be overridden in subclasses.
     *
     * @param interceptor the specified interceptor object
     * @return the interceptor wrapped as HandlerInterceptor
     * @see org.springframework.web.servlet.HandlerInterceptor
     * @see org.springframework.web.context.request.WebRequestInterceptor
     * @see WebRequestHandlerInterceptorAdapter
     */
    protected HandlerInterceptor adaptInterceptor(Object interceptor) {
        if (interceptor instanceof HandlerInterceptor) {
            return (HandlerInterceptor) interceptor;
        } else if (interceptor instanceof WebRequestInterceptor) {
            return new WebRequestHandlerInterceptorAdapter((WebRequestInterceptor) interceptor);
        } else {
            throw new IllegalArgumentException("Interceptor type not supported: " + interceptor.getClass().getName());
        }
    }

    /**
     * Return the adapted interceptors as {@link HandlerInterceptor} array.
     *
     * @return the array of {@link HandlerInterceptor}s, or {@code null} if none
     */
    protected final HandlerInterceptor[] getAdaptedInterceptors() {
        int count = this.adaptedInterceptors.size();
        return (count > 0 ? this.adaptedInterceptors.toArray(new HandlerInterceptor[count]) : null);
    }

    /**
     * Return all configured {@link MappedInterceptor}s as an array.
     *
     * @return the array of {@link MappedInterceptor}s, or {@code null} if none
     */
    protected final MappedInterceptor[] getMappedInterceptors() {
        List<MappedInterceptor> mappedInterceptors = new ArrayList<MappedInterceptor>();
        for (HandlerInterceptor interceptor : this.adaptedInterceptors) {
            if (interceptor instanceof MappedInterceptor) {
                mappedInterceptors.add((MappedInterceptor) interceptor);
            }
        }
        int count = mappedInterceptors.size();
        return (count > 0 ? mappedInterceptors.toArray(new MappedInterceptor[count]) : null);
    }


    //获取处理器
    @Override
    public final HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        //根据请求获取处理器
        Object handler = getHandlerInternal(request);
        if (handler == null) {
            handler = getDefaultHandler();
        }
        if (handler == null) {
            return null;
        }
        // Bean name or resolved handler?
        if (handler instanceof String) {
            String handlerName = (String) handler;
            handler = getApplicationContext().getBean(handlerName);
        }

        HandlerExecutionChain executionChain = getHandlerExecutionChain(handler, request);
        if (CorsUtils.isCorsRequest(request)) {
            CorsConfiguration globalConfig = this.globalCorsConfigSource.getCorsConfiguration(request);
            CorsConfiguration handlerConfig = getCorsConfiguration(handler, request);
            CorsConfiguration config = (globalConfig != null ? globalConfig.combine(handlerConfig) : handlerConfig);
            executionChain = getCorsHandlerExecutionChain(request, executionChain, config);
        }
        return executionChain;
    }

    //根据请求获取处理器
    protected abstract Object getHandlerInternal(HttpServletRequest request) throws Exception;

    //获取处理器执行链
    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
        HandlerExecutionChain chain = (handler instanceof HandlerExecutionChain ? (HandlerExecutionChain) handler : new HandlerExecutionChain(handler));
        String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
        for (HandlerInterceptor interceptor : this.adaptedInterceptors) {
            if (interceptor instanceof MappedInterceptor) {
                MappedInterceptor mappedInterceptor = (MappedInterceptor) interceptor;
                if (mappedInterceptor.matches(lookupPath, this.pathMatcher)) {
                    chain.addInterceptor(mappedInterceptor.getInterceptor());
                }
            } else {
                chain.addInterceptor(interceptor);
            }
        }
        return chain;
    }

    /**
     * Retrieve the CORS configuration for the given handler.
     *
     * @param handler the handler to check (never {@code null}).
     * @param request the current request.
     * @return the CORS configuration for the handler, or {@code null} if none
     * @since 4.2
     */
    protected CorsConfiguration getCorsConfiguration(Object handler, HttpServletRequest request) {
        Object resolvedHandler = handler;
        if (handler instanceof HandlerExecutionChain) {
            resolvedHandler = ((HandlerExecutionChain) handler).getHandler();
        }
        if (resolvedHandler instanceof CorsConfigurationSource) {
            return ((CorsConfigurationSource) resolvedHandler).getCorsConfiguration(request);
        }
        return null;
    }

    /**
     * Update the HandlerExecutionChain for CORS-related handling.
     * <p>For pre-flight requests, the default implementation replaces the selected
     * handler with a simple HttpRequestHandler that invokes the configured
     * {@link #setCorsProcessor}.
     * <p>For actual requests, the default implementation inserts a
     * HandlerInterceptor that makes CORS-related checks and adds CORS headers.
     *
     * @param request the current request
     * @param chain   the handler chain
     * @param config  the applicable CORS configuration (possibly {@code null})
     * @since 4.2
     */
    protected HandlerExecutionChain getCorsHandlerExecutionChain(HttpServletRequest request,
                                                                 HandlerExecutionChain chain, CorsConfiguration config) {
        if (CorsUtils.isPreFlightRequest(request)) {
            HandlerInterceptor[] interceptors = chain.getInterceptors();
            chain = new HandlerExecutionChain(new PreFlightHandler(config), interceptors);
        } else {
            chain.addInterceptor(new CorsInterceptor(config));
        }
        return chain;
    }


    private class PreFlightHandler implements HttpRequestHandler, CorsConfigurationSource {

        private final CorsConfiguration config;

        public PreFlightHandler(CorsConfiguration config) {
            this.config = config;
        }

        @Override
        public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
            corsProcessor.processRequest(this.config, request, response);
        }

        @Override
        public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
            return this.config;
        }
    }


    private class CorsInterceptor extends HandlerInterceptorAdapter implements CorsConfigurationSource {

        private final CorsConfiguration config;

        public CorsInterceptor(CorsConfiguration config) {
            this.config = config;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws Exception {

            return corsProcessor.processRequest(this.config, request, response);
        }

        @Override
        public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
            return this.config;
        }
    }

}
