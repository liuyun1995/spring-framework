package org.springframework.web.servlet.mvc;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

/**
 * Spring Controller implementation that forwards to a named servlet,
 * i.e. the "servlet-name" in web.xml rather than a URL path mapping.
 * A target servlet doesn't even need a "servlet-mapping" in web.xml
 * in the first place: A "servlet" declaration is sufficient.
 *
 * <p>Useful to invoke an existing servlet via Spring's dispatching infrastructure,
 * for example to apply Spring HandlerInterceptors to its requests. This will work
 * even in a minimal Servlet container that does not support Servlet filters.
 *
 * <p><b>Example:</b> web.xml, mapping all "/myservlet" requests to a Spring dispatcher.
 * Also defines a custom "myServlet", but <i>without</i> servlet mapping.
 *
 * <pre class="code">
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;myServlet&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;mypackage.TestServlet&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 *
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;myDispatcher&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;org.springframework.web.servlet.DispatcherServlet&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 *
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;myDispatcher&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;/myservlet&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;</pre>
 *
 * <b>Example:</b> myDispatcher-servlet.xml, in turn forwarding "/myservlet" to your
 * servlet (identified by servlet name). All such requests will go through the
 * configured HandlerInterceptor chain (e.g. an OpenSessionInViewInterceptor).
 * From the servlet point of view, everything will work as usual.
 *
 * <pre class="code">
 * &lt;bean id="urlMapping" class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping"&gt;
 *   &lt;property name="interceptors"&gt;
 *     &lt;list&gt;
 *       &lt;ref bean="openSessionInViewInterceptor"/&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 *   &lt;property name="mappings"&gt;
 *     &lt;props&gt;
 *       &lt;prop key="/myservlet"&gt;myServletForwardingController&lt;/prop&gt;
 *     &lt;/props&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="myServletForwardingController" class="org.springframework.web.servlet.mvc.ServletForwardingController"&gt;
 *   &lt;property name="servletName"&gt;&lt;value&gt;myServlet&lt;/value&gt;&lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Juergen Hoeller
 * @see ServletWrappingController
 * @see org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor
 * @see org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter
 * @since 1.1.1
 */
public class ServletForwardingController extends AbstractController implements BeanNameAware {

    private String servletName;
    private String beanName;

    //构造器
    public ServletForwardingController() {
        super(false);
    }

    //设置Servlet名称
    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    //设置Bean名称
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        if (this.servletName == null) {
            this.servletName = name;
        }
    }

    //处理请求
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        RequestDispatcher rd = getServletContext().getNamedDispatcher(this.servletName);
        if (rd == null) {
            throw new ServletException("No servlet with name '" + this.servletName + "' defined in web.xml");
        }
        // If already included, include again, else forward.
        if (useInclude(request, response)) {
            rd.include(request, response);
            if (logger.isDebugEnabled()) {
                logger.debug("Included servlet [" + this.servletName +
                        "] in ServletForwardingController '" + this.beanName + "'");
            }
        } else {
            rd.forward(request, response);
            if (logger.isDebugEnabled()) {
                logger.debug("Forwarded to servlet [" + this.servletName +
                        "] in ServletForwardingController '" + this.beanName + "'");
            }
        }
        return null;
    }

    /**
     * Determine whether to use RequestDispatcher's {@code include} or
     * {@code forward} method.
     * <p>Performs a check whether an include URI attribute is found in the request,
     * indicating an include request, and whether the response has already been committed.
     * In both cases, an include will be performed, as a forward is not possible anymore.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @return {@code true} for include, {@code false} for forward
     * @see javax.servlet.RequestDispatcher#forward
     * @see javax.servlet.RequestDispatcher#include
     * @see javax.servlet.ServletResponse#isCommitted
     * @see org.springframework.web.util.WebUtils#isIncludeRequest
     */
    protected boolean useInclude(HttpServletRequest request, HttpServletResponse response) {
        return (WebUtils.isIncludeRequest(request) || response.isCommitted());
    }

}
