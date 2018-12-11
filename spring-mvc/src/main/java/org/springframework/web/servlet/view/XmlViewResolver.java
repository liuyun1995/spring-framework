package org.springframework.web.servlet.view;

import java.util.Locale;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.View;

//XML视图解析器
public class XmlViewResolver extends AbstractCachingViewResolver
        implements Ordered, InitializingBean, DisposableBean {

    /**
     * Default if no other location is supplied
     */
    public final static String DEFAULT_LOCATION = "/WEB-INF/views.xml";

    private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

    private Resource location;

    private ConfigurableApplicationContext cachedFactory;

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * Set the location of the XML file that defines the view beans.
     * <p>The default is "/WEB-INF/views.xml".
     *
     * @param location the location of the XML file.
     */
    public void setLocation(Resource location) {
        this.location = location;
    }

    /**
     * Pre-initialize the factory from the XML file.
     * Only effective if caching is enabled.
     */
    @Override
    public void afterPropertiesSet() throws BeansException {
        if (isCache()) {
            initFactory();
        }
    }


    /**
     * This implementation returns just the view name,
     * as XmlViewResolver doesn't support localized resolution.
     */
    @Override
    protected Object getCacheKey(String viewName, Locale locale) {
        return viewName;
    }

    @Override
    protected View loadView(String viewName, Locale locale) throws BeansException {
        BeanFactory factory = initFactory();
        try {
            return factory.getBean(viewName, View.class);
        } catch (NoSuchBeanDefinitionException ex) {
            // Allow for ViewResolver chaining...
            return null;
        }
    }

    /**
     * Initialize the view bean factory from the XML file.
     * Synchronized because of access by parallel threads.
     *
     * @throws BeansException in case of initialization errors
     */
    protected synchronized BeanFactory initFactory() throws BeansException {
        if (this.cachedFactory != null) {
            return this.cachedFactory;
        }

        Resource actualLocation = this.location;
        if (actualLocation == null) {
            actualLocation = getApplicationContext().getResource(DEFAULT_LOCATION);
        }

        // Create child ApplicationContext for views.
        GenericWebApplicationContext factory = new GenericWebApplicationContext();
        factory.setParent(getApplicationContext());
        factory.setServletContext(getServletContext());

        // Load XML resource with context-aware entity resolver.
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.setEnvironment(getApplicationContext().getEnvironment());
        reader.setEntityResolver(new ResourceEntityResolver(getApplicationContext()));
        reader.loadBeanDefinitions(actualLocation);

        factory.refresh();

        if (isCache()) {
            this.cachedFactory = factory;
        }
        return factory;
    }


    /**
     * Close the view bean factory on context shutdown.
     */
    @Override
    public void destroy() throws BeansException {
        if (this.cachedFactory != null) {
            this.cachedFactory.close();
        }
    }

}
