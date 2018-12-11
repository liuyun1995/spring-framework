package org.springframework.web.servlet.handler;

import java.util.Collections;
import java.util.Enumeration;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;

//简单Servlet后置处理器
public class SimpleServletPostProcessor implements
        DestructionAwareBeanPostProcessor, ServletContextAware, ServletConfigAware {

    private boolean useSharedServletConfig = true;

    private ServletContext servletContext;

    private ServletConfig servletConfig;

    //设置使用共享的Servlet配置
    public void setUseSharedServletConfig(boolean useSharedServletConfig) {
        this.useSharedServletConfig = useSharedServletConfig;
    }

    //设置Servlet上下文
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    //设置Servlet配置
    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Servlet) {
            ServletConfig config = this.servletConfig;
            if (config == null || !this.useSharedServletConfig) {
                config = new DelegatingServletConfig(beanName, this.servletContext);
            }
            try {
                ((Servlet) bean).init(config);
            } catch (ServletException ex) {
                throw new BeanInitializationException("Servlet.init threw exception", ex);
            }
        }
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (bean instanceof Servlet) {
            ((Servlet) bean).destroy();
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return (bean instanceof Servlet);
    }


    /**
     * Internal implementation of the {@link ServletConfig} interface,
     * to be passed to the wrapped servlet.
     */
    private static class DelegatingServletConfig implements ServletConfig {

        private final String servletName;

        private final ServletContext servletContext;

        public DelegatingServletConfig(String servletName, ServletContext servletContext) {
            this.servletName = servletName;
            this.servletContext = servletContext;
        }

        @Override
        public String getServletName() {
            return this.servletName;
        }

        @Override
        public ServletContext getServletContext() {
            return this.servletContext;
        }

        @Override
        public String getInitParameter(String paramName) {
            return null;
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return Collections.enumeration(Collections.<String>emptySet());
        }
    }

}
