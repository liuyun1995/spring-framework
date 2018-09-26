package org.springframework.web.servlet.support;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleTimeZoneAwareLocaleContext;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.EscapedErrors;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriTemplate;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;
import java.util.*;

//请求上下文
public class RequestContext {

    //默认主题名称
    public static final String DEFAULT_THEME_NAME = "theme";

    /**
     * Request attribute to hold the current web application context for RequestContext usage.
     * By default, the DispatcherServlet's context (or the root context as fallback) is exposed.
     */
    public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = RequestContext.class.getName() + ".CONTEXT";


    protected static final boolean jstlPresent = ClassUtils.isPresent(
            "javax.servlet.jsp.jstl.core.Config", RequestContext.class.getClassLoader());

    private HttpServletRequest request;

    private HttpServletResponse response;

    private Map<String, Object> model;

    private WebApplicationContext webApplicationContext;

    private Locale locale;

    private TimeZone timeZone;

    private Theme theme;

    private Boolean defaultHtmlEscape;

    private Boolean responseEncodedHtmlEscape;

    private UrlPathHelper urlPathHelper;

    private RequestDataValueProcessor requestDataValueProcessor;

    private Map<String, Errors> errorsMap;


    public RequestContext(HttpServletRequest request) {
        initContext(request, null, null, null);
    }

    public RequestContext(HttpServletRequest request, HttpServletResponse response) {
        initContext(request, response, null, null);
    }

    public RequestContext(HttpServletRequest request, ServletContext servletContext) {
        initContext(request, null, servletContext, null);
    }

    public RequestContext(HttpServletRequest request, Map<String, Object> model) {
        initContext(request, null, null, model);
    }

    public RequestContext(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext,
                          Map<String, Object> model) {

        initContext(request, response, servletContext, model);
    }

    protected RequestContext() {
    }


    //初始化上下文
    protected void initContext(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext,
                               Map<String, Object> model) {

        this.request = request;
        this.response = response;
        this.model = model;

        // Fetch WebApplicationContext, either from DispatcherServlet or the root context.
        // ServletContext needs to be specified to be able to fall back to the root context!
        this.webApplicationContext = (WebApplicationContext) request.getAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (this.webApplicationContext == null) {
            this.webApplicationContext = RequestContextUtils.findWebApplicationContext(request, servletContext);
            if (this.webApplicationContext == null) {
                throw new IllegalStateException("No WebApplicationContext found: not in a DispatcherServlet " +
                        "request and no ContextLoaderListener registered?");
            }
        }

        // Determine locale to use for this RequestContext.
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        if (localeResolver instanceof LocaleContextResolver) {
            LocaleContext localeContext = ((LocaleContextResolver) localeResolver).resolveLocaleContext(request);
            this.locale = localeContext.getLocale();
            if (localeContext instanceof TimeZoneAwareLocaleContext) {
                this.timeZone = ((TimeZoneAwareLocaleContext) localeContext).getTimeZone();
            }
        } else if (localeResolver != null) {
            // Try LocaleResolver (we're within a DispatcherServlet request).
            this.locale = localeResolver.resolveLocale(request);
        }

        // Try JSTL fallbacks if necessary.
        if (this.locale == null) {
            this.locale = getFallbackLocale();
        }
        if (this.timeZone == null) {
            this.timeZone = getFallbackTimeZone();
        }

        // Determine default HTML escape setting from the "defaultHtmlEscape"
        // context-param in web.xml, if any.
        this.defaultHtmlEscape = WebUtils.getDefaultHtmlEscape(this.webApplicationContext.getServletContext());

        // Determine response-encoded HTML escape setting from the "responseEncodedHtmlEscape"
        // context-param in web.xml, if any.
        this.responseEncodedHtmlEscape = WebUtils.getResponseEncodedHtmlEscape(this.webApplicationContext.getServletContext());

        this.urlPathHelper = new UrlPathHelper();

        if (this.webApplicationContext.containsBean(RequestContextUtils.REQUEST_DATA_VALUE_PROCESSOR_BEAN_NAME)) {
            this.requestDataValueProcessor = this.webApplicationContext.getBean(
                    RequestContextUtils.REQUEST_DATA_VALUE_PROCESSOR_BEAN_NAME, RequestDataValueProcessor.class);
        }
    }


    //获取请求
    protected final HttpServletRequest getRequest() {
        return this.request;
    }

    //获取Servlet上下文
    protected final ServletContext getServletContext() {
        return this.webApplicationContext.getServletContext();
    }

    //获取应用上下文
    public final WebApplicationContext getWebApplicationContext() {
        return this.webApplicationContext;
    }

    //获取消息源
    public final MessageSource getMessageSource() {
        return this.webApplicationContext;
    }

    //获取模型
    public final Map<String, Object> getModel() {
        return this.model;
    }

    /**
     * Return the current Locale (falling back to the request locale; never {@code null}).
     * <p>Typically coming from a DispatcherServlet's {@link LocaleResolver}.
     * Also includes a fallback check for JSTL's Locale attribute.
     *
     * @see RequestContextUtils#getLocale
     */
    public final Locale getLocale() {
        return this.locale;
    }

    /**
     * Return the current TimeZone (or {@code null} if none derivable from the request).
     * <p>Typically coming from a DispatcherServlet's {@link LocaleContextResolver}.
     * Also includes a fallback check for JSTL's TimeZone attribute.
     *
     * @see RequestContextUtils#getTimeZone
     */
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    /**
     * Determine the fallback locale for this context.
     * <p>The default implementation checks for a JSTL locale attribute in request, session
     * or application scope; if not found, returns the {@code HttpServletRequest.getLocale()}.
     *
     * @return the fallback locale (never {@code null})
     * @see javax.servlet.http.HttpServletRequest#getLocale()
     */
    protected Locale getFallbackLocale() {
        if (jstlPresent) {
            Locale locale = JstlLocaleResolver.getJstlLocale(getRequest(), getServletContext());
            if (locale != null) {
                return locale;
            }
        }
        return getRequest().getLocale();
    }

    /**
     * Determine the fallback time zone for this context.
     * <p>The default implementation checks for a JSTL time zone attribute in request,
     * session or application scope; returns {@code null} if not found.
     *
     * @return the fallback time zone (or {@code null} if none derivable from the request)
     */
    protected TimeZone getFallbackTimeZone() {
        if (jstlPresent) {
            TimeZone timeZone = JstlLocaleResolver.getJstlTimeZone(getRequest(), getServletContext());
            if (timeZone != null) {
                return timeZone;
            }
        }
        return null;
    }

    /**
     * Change the current locale to the specified one,
     * storing the new locale through the configured {@link LocaleResolver}.
     *
     * @param locale the new locale
     * @see LocaleResolver#setLocale
     * @see #changeLocale(java.util.Locale, java.util.TimeZone)
     */
    public void changeLocale(Locale locale) {
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(this.request);
        if (localeResolver == null) {
            throw new IllegalStateException("Cannot change locale if no LocaleResolver configured");
        }
        localeResolver.setLocale(this.request, this.response, locale);
        this.locale = locale;
    }

    /**
     * Change the current locale to the specified locale and time zone context,
     * storing the new locale context through the configured {@link LocaleResolver}.
     *
     * @param locale   the new locale
     * @param timeZone the new time zone
     * @see LocaleContextResolver#setLocaleContext
     * @see org.springframework.context.i18n.SimpleTimeZoneAwareLocaleContext
     */
    public void changeLocale(Locale locale, TimeZone timeZone) {
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(this.request);
        if (!(localeResolver instanceof LocaleContextResolver)) {
            throw new IllegalStateException("Cannot change locale context if no LocaleContextResolver configured");
        }
        ((LocaleContextResolver) localeResolver).setLocaleContext(this.request, this.response,
                new SimpleTimeZoneAwareLocaleContext(locale, timeZone));
        this.locale = locale;
        this.timeZone = timeZone;
    }

    /**
     * Return the current theme (never {@code null}).
     * <p>Resolved lazily for more efficiency when theme support is not being used.
     */
    public Theme getTheme() {
        if (this.theme == null) {
            // Lazily determine theme to use for this RequestContext.
            this.theme = RequestContextUtils.getTheme(this.request);
            if (this.theme == null) {
                // No ThemeResolver and ThemeSource available -> try fallback.
                this.theme = getFallbackTheme();
            }
        }
        return this.theme;
    }

    /**
     * Determine the fallback theme for this context.
     * <p>The default implementation returns the default theme (with name "theme").
     *
     * @return the fallback theme (never {@code null})
     */
    protected Theme getFallbackTheme() {
        ThemeSource themeSource = RequestContextUtils.getThemeSource(getRequest());
        if (themeSource == null) {
            themeSource = new ResourceBundleThemeSource();
        }
        Theme theme = themeSource.getTheme(DEFAULT_THEME_NAME);
        if (theme == null) {
            throw new IllegalStateException("No theme defined and no fallback theme found");
        }
        return theme;
    }

    /**
     * Change the current theme to the specified one,
     * storing the new theme name through the configured {@link ThemeResolver}.
     *
     * @param theme the new theme
     * @see ThemeResolver#setThemeName
     */
    public void changeTheme(Theme theme) {
        ThemeResolver themeResolver = RequestContextUtils.getThemeResolver(this.request);
        if (themeResolver == null) {
            throw new IllegalStateException("Cannot change theme if no ThemeResolver configured");
        }
        themeResolver.setThemeName(this.request, this.response, (theme != null ? theme.getName() : null));
        this.theme = theme;
    }

    /**
     * Change the current theme to the specified theme by name,
     * storing the new theme name through the configured {@link ThemeResolver}.
     *
     * @param themeName the name of the new theme
     * @see ThemeResolver#setThemeName
     */
    public void changeTheme(String themeName) {
        ThemeResolver themeResolver = RequestContextUtils.getThemeResolver(this.request);
        if (themeResolver == null) {
            throw new IllegalStateException("Cannot change theme if no ThemeResolver configured");
        }
        themeResolver.setThemeName(this.request, this.response, themeName);
        // Ask for re-resolution on next getTheme call.
        this.theme = null;
    }

    /**
     * (De)activate default HTML escaping for messages and errors, for the scope of this RequestContext.
     * <p>The default is the application-wide setting (the "defaultHtmlEscape" context-param in web.xml).
     *
     * @see org.springframework.web.util.WebUtils#getDefaultHtmlEscape
     */
    public void setDefaultHtmlEscape(boolean defaultHtmlEscape) {
        this.defaultHtmlEscape = defaultHtmlEscape;
    }

    /**
     * Is default HTML escaping active? Falls back to {@code false} in case of no explicit default given.
     */
    public boolean isDefaultHtmlEscape() {
        return (this.defaultHtmlEscape != null && this.defaultHtmlEscape.booleanValue());
    }

    /**
     * Return the default HTML escape setting, differentiating between no default specified and an explicit value.
     *
     * @return whether default HTML escaping is enabled (null = no explicit default)
     */
    public Boolean getDefaultHtmlEscape() {
        return this.defaultHtmlEscape;
    }

    /**
     * Is HTML escaping using the response encoding by default?
     * If enabled, only XML markup significant characters will be escaped with UTF-* encodings.
     * <p>Falls back to {@code true} in case of no explicit default given, as of Spring 4.2.
     *
     * @since 4.1.2
     */
    public boolean isResponseEncodedHtmlEscape() {
        return (this.responseEncodedHtmlEscape == null || this.responseEncodedHtmlEscape.booleanValue());
    }

    /**
     * Return the default setting about use of response encoding for HTML escape setting,
     * differentiating between no default specified and an explicit value.
     *
     * @return whether default use of response encoding HTML escaping is enabled (null = no explicit default)
     * @since 4.1.2
     */
    public Boolean getResponseEncodedHtmlEscape() {
        return this.responseEncodedHtmlEscape;
    }


    /**
     * Set the UrlPathHelper to use for context path and request URI decoding.
     * Can be used to pass a shared UrlPathHelper instance in.
     * <p>A default UrlPathHelper is always available.
     */
    public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
        this.urlPathHelper = urlPathHelper;
    }

    /**
     * Return the UrlPathHelper used for context path and request URI decoding.
     * Can be used to configure the current UrlPathHelper.
     * <p>A default UrlPathHelper is always available.
     */
    public UrlPathHelper getUrlPathHelper() {
        return this.urlPathHelper;
    }

    /**
     * Return the RequestDataValueProcessor instance to use obtained from the
     * WebApplicationContext under the name {@code "requestDataValueProcessor"}.
     * Or {@code null} if no matching bean was found.
     */
    public RequestDataValueProcessor getRequestDataValueProcessor() {
        return this.requestDataValueProcessor;
    }

    //获取上下文路径
    public String getContextPath() {
        return this.urlPathHelper.getOriginatingContextPath(this.request);
    }

    //获取上下文URL
    public String getContextUrl(String relativeUrl) {
        String url = getContextPath() + relativeUrl;
        if (this.response != null) {
            url = this.response.encodeURL(url);
        }
        return url;
    }

    //获取上下文URL
    public String getContextUrl(String relativeUrl, Map<String, ?> params) {
        String url = getContextPath() + relativeUrl;
        UriTemplate template = new UriTemplate(url);
        url = template.expand(params).toASCIIString();
        if (this.response != null) {
            url = this.response.encodeURL(url);
        }
        return url;
    }

    /**
     * Return the path to URL mappings within the current servlet including the
     * context path and the servlet path of the original request. This is useful
     * for building links to other resources within the application where a
     * servlet mapping of the style {@code "/main/*"} is used.
     * <p>Delegates to the UrlPathHelper to determine the context and servlet path.
     */
    public String getPathToServlet() {
        String path = this.urlPathHelper.getOriginatingContextPath(this.request);
        if (StringUtils.hasText(this.urlPathHelper.getPathWithinServletMapping(this.request))) {
            path += this.urlPathHelper.getOriginatingServletPath(this.request);
        }
        return path;
    }

    //获取请求URI
    public String getRequestUri() {
        return this.urlPathHelper.getOriginatingRequestUri(this.request);
    }

    //获取查询字符串
    public String getQueryString() {
        return this.urlPathHelper.getOriginatingQueryString(this.request);
    }

    //获取消息
    public String getMessage(String code, String defaultMessage) {
        return getMessage(code, null, defaultMessage, isDefaultHtmlEscape());
    }

    //获取消息
    public String getMessage(String code, Object[] args, String defaultMessage) {
        return getMessage(code, args, defaultMessage, isDefaultHtmlEscape());
    }

    //获取消息
    public String getMessage(String code, List<?> args, String defaultMessage) {
        return getMessage(code, (args != null ? args.toArray() : null), defaultMessage, isDefaultHtmlEscape());
    }

    //获取消息
    public String getMessage(String code, Object[] args, String defaultMessage, boolean htmlEscape) {
        String msg = this.webApplicationContext.getMessage(code, args, defaultMessage, this.locale);
        return (htmlEscape ? HtmlUtils.htmlEscape(msg) : msg);
    }

    //获取消息
    public String getMessage(String code) throws NoSuchMessageException {
        return getMessage(code, null, isDefaultHtmlEscape());
    }

    //获取消息
    public String getMessage(String code, Object[] args) throws NoSuchMessageException {
        return getMessage(code, args, isDefaultHtmlEscape());
    }

    //获取消息
    public String getMessage(String code, List<?> args) throws NoSuchMessageException {
        return getMessage(code, (args != null ? args.toArray() : null), isDefaultHtmlEscape());
    }

    //获取消息
    public String getMessage(String code, Object[] args, boolean htmlEscape) throws NoSuchMessageException {
        String msg = this.webApplicationContext.getMessage(code, args, this.locale);
        return (htmlEscape ? HtmlUtils.htmlEscape(msg) : msg);
    }

    //获取消息
    public String getMessage(MessageSourceResolvable resolvable) throws NoSuchMessageException {
        return getMessage(resolvable, isDefaultHtmlEscape());
    }

    //获取消息
    public String getMessage(MessageSourceResolvable resolvable, boolean htmlEscape) throws NoSuchMessageException {
        String msg = this.webApplicationContext.getMessage(resolvable, this.locale);
        return (htmlEscape ? HtmlUtils.htmlEscape(msg) : msg);
    }

    //获取主题消息
    public String getThemeMessage(String code, String defaultMessage) {
        return getTheme().getMessageSource().getMessage(code, null, defaultMessage, this.locale);
    }

    //获取主题消息
    public String getThemeMessage(String code, Object[] args, String defaultMessage) {
        return getTheme().getMessageSource().getMessage(code, args, defaultMessage, this.locale);
    }

    //获取主题消息
    public String getThemeMessage(String code, List<?> args, String defaultMessage) {
        return getTheme().getMessageSource().getMessage(code, (args != null ? args.toArray() : null), defaultMessage,
                this.locale);
    }

    //获取主题消息
    public String getThemeMessage(String code) throws NoSuchMessageException {
        return getTheme().getMessageSource().getMessage(code, null, this.locale);
    }

    //获取主题消息
    public String getThemeMessage(String code, Object[] args) throws NoSuchMessageException {
        return getTheme().getMessageSource().getMessage(code, args, this.locale);
    }

    //获取主题消息
    public String getThemeMessage(String code, List<?> args) throws NoSuchMessageException {
        return getTheme().getMessageSource().getMessage(code, (args != null ? args.toArray() : null), this.locale);
    }

    //获取主题消息
    public String getThemeMessage(MessageSourceResolvable resolvable) throws NoSuchMessageException {
        return getTheme().getMessageSource().getMessage(resolvable, this.locale);
    }

    //获取错误消息
    public Errors getErrors(String name) {
        return getErrors(name, isDefaultHtmlEscape());
    }

    //获取错误消息
    public Errors getErrors(String name, boolean htmlEscape) {
        if (this.errorsMap == null) {
            this.errorsMap = new HashMap<String, Errors>();
        }
        Errors errors = this.errorsMap.get(name);
        boolean put = false;
        if (errors == null) {
            errors = (Errors) getModelObject(BindingResult.MODEL_KEY_PREFIX + name);
            // Check old BindException prefix for backwards compatibility.
            if (errors instanceof BindException) {
                errors = ((BindException) errors).getBindingResult();
            }
            if (errors == null) {
                return null;
            }
            put = true;
        }
        if (htmlEscape && !(errors instanceof EscapedErrors)) {
            errors = new EscapedErrors(errors);
            put = true;
        } else if (!htmlEscape && errors instanceof EscapedErrors) {
            errors = ((EscapedErrors) errors).getSource();
            put = true;
        }
        if (put) {
            this.errorsMap.put(name, errors);
        }
        return errors;
    }

    //获取模型对象
    protected Object getModelObject(String modelName) {
        if (this.model != null) {
            return this.model.get(modelName);
        } else {
            return this.request.getAttribute(modelName);
        }
    }

    //获取绑定状态
    public BindStatus getBindStatus(String path) throws IllegalStateException {
        return new BindStatus(this, path, isDefaultHtmlEscape());
    }

    //获取绑定状态
    public BindStatus getBindStatus(String path, boolean htmlEscape) throws IllegalStateException {
        return new BindStatus(this, path, htmlEscape);
    }


    /**
     * Inner class that isolates the JSTL dependency.
     * Just called to resolve the fallback locale if the JSTL API is present.
     */
    private static class JstlLocaleResolver {

        public static Locale getJstlLocale(HttpServletRequest request, ServletContext servletContext) {
            Object localeObject = Config.get(request, Config.FMT_LOCALE);
            if (localeObject == null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    localeObject = Config.get(session, Config.FMT_LOCALE);
                }
                if (localeObject == null && servletContext != null) {
                    localeObject = Config.get(servletContext, Config.FMT_LOCALE);
                }
            }
            return (localeObject instanceof Locale ? (Locale) localeObject : null);
        }

        public static TimeZone getJstlTimeZone(HttpServletRequest request, ServletContext servletContext) {
            Object timeZoneObject = Config.get(request, Config.FMT_TIME_ZONE);
            if (timeZoneObject == null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    timeZoneObject = Config.get(session, Config.FMT_TIME_ZONE);
                }
                if (timeZoneObject == null && servletContext != null) {
                    timeZoneObject = Config.get(servletContext, Config.FMT_TIME_ZONE);
                }
            }
            return (timeZoneObject instanceof TimeZone ? (TimeZone) timeZoneObject : null);
        }
    }

}
