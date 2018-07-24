package org.springframework.aop.framework.autoproxy;

import org.springframework.core.NamedThreadLocal;

public class ProxyCreationContext {

    private static final ThreadLocal<String> currentProxiedBeanName = new NamedThreadLocal<String>("Name of currently proxied bean");

    public static String getCurrentProxiedBeanName() {
        return currentProxiedBeanName.get();
    }

    static void setCurrentProxiedBeanName(String beanName) {
        if (beanName != null) {
            currentProxiedBeanName.set(beanName);
        } else {
            currentProxiedBeanName.remove();
        }
    }

}
