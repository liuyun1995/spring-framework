package org.springframework.web.servlet.handler;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.ObjectUtils;

//重定向URLc处理器映射
public abstract class AbstractDetectingUrlHandlerMapping extends AbstractUrlHandlerMapping {

    private boolean detectHandlersInAncestorContexts = false;


    /**
     * Set whether to detect handler beans in ancestor ApplicationContexts.
     * <p>Default is "false": Only handler beans in the current ApplicationContext
     * will be detected, i.e. only in the context that this HandlerMapping itself
     * is defined in (typically the current DispatcherServlet's context).
     * <p>Switch this flag on to detect handler beans in ancestor contexts
     * (typically the Spring root WebApplicationContext) as well.
     */
    public void setDetectHandlersInAncestorContexts(boolean detectHandlersInAncestorContexts) {
        this.detectHandlersInAncestorContexts = detectHandlersInAncestorContexts;
    }

    //初始化应用上下文
    @Override
    public void initApplicationContext() throws ApplicationContextException {
        super.initApplicationContext();
        detectHandlers();
    }

    //重定向处理器
    protected void detectHandlers() throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking for URL mappings in application context: " + getApplicationContext());
        }
        String[] beanNames = (this.detectHandlersInAncestorContexts ?
                BeanFactoryUtils.beanNamesForTypeIncludingAncestors(getApplicationContext(), Object.class) :
                getApplicationContext().getBeanNamesForType(Object.class));

        // Take any bean name that we can determine URLs for.
        for (String beanName : beanNames) {
            String[] urls = determineUrlsForHandler(beanName);
            if (!ObjectUtils.isEmpty(urls)) {
                // URL paths found: Let's consider it a handler.
                registerHandler(urls, beanName);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Rejected bean name '" + beanName + "': no URL paths identified");
                }
            }
        }
    }

    //确定URL根据处理器
    protected abstract String[] determineUrlsForHandler(String beanName);

}
