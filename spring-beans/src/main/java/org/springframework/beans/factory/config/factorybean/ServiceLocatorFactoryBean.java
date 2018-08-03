package org.springframework.beans.factory.config.factorybean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.factorybean.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public class ServiceLocatorFactoryBean implements FactoryBean<Object>, BeanFactoryAware, InitializingBean {

    private Class<?> serviceLocatorInterface;
    private Constructor<Exception> serviceLocatorExceptionConstructor;
    private Properties serviceMappings;
    private ListableBeanFactory beanFactory;
    private Object proxy;

    public void setServiceLocatorInterface(Class<?> interfaceType) {
        this.serviceLocatorInterface = interfaceType;
    }

    public void setServiceLocatorExceptionClass(Class<? extends Exception> serviceLocatorExceptionClass) {
        if (serviceLocatorExceptionClass != null && !Exception.class.isAssignableFrom(serviceLocatorExceptionClass)) {
            throw new IllegalArgumentException(
                    "serviceLocatorException [" + serviceLocatorExceptionClass.getName() + "] is not a subclass of Exception");
        }
        this.serviceLocatorExceptionConstructor =
                determineServiceLocatorExceptionConstructor(serviceLocatorExceptionClass);
    }

    /**
     * Set mappings between service ids (passed into the service locator)
     * and bean names (in the bean factory). Service ids that are not defined
     * here will be treated as bean names as-is.
     * <p>The empty string as service id key defines the mapping for {@code null} and
     * empty string, and for factory methods without parameter. If not defined,
     * a single matching bean will be retrieved from the bean factory.
     *
     * @param serviceMappings mappings between service ids and bean names,
     *                        with service ids as keys as bean names as values
     */
    public void setServiceMappings(Properties serviceMappings) {
        this.serviceMappings = serviceMappings;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof ListableBeanFactory)) {
            throw new FatalBeanException(
                    "ServiceLocatorFactoryBean needs to run in a BeanFactory that is a ListableBeanFactory");
        }
        this.beanFactory = (ListableBeanFactory) beanFactory;
    }

    @Override
    public void afterPropertiesSet() {
        if (this.serviceLocatorInterface == null) {
            throw new IllegalArgumentException("Property 'serviceLocatorInterface' is required");
        }

        // Create service locator proxy.
        this.proxy = Proxy.newProxyInstance(
                this.serviceLocatorInterface.getClassLoader(),
                new Class<?>[]{this.serviceLocatorInterface},
                new ServiceLocatorInvocationHandler());
    }


    /**
     * Determine the constructor to use for the given service locator exception
     * class. Only called in case of a custom service locator exception.
     * <p>The default implementation looks for a constructor with one of the
     * following parameter types: {@code (String, Throwable)}
     * or {@code (Throwable)} or {@code (String)}.
     *
     * @param exceptionClass the exception class
     * @return the constructor to use
     * @see #setServiceLocatorExceptionClass
     */
    @SuppressWarnings("unchecked")
    protected Constructor<Exception> determineServiceLocatorExceptionConstructor(Class<? extends Exception> exceptionClass) {
        try {
            return (Constructor<Exception>) exceptionClass.getConstructor(String.class, Throwable.class);
        } catch (NoSuchMethodException ex) {
            try {
                return (Constructor<Exception>) exceptionClass.getConstructor(Throwable.class);
            } catch (NoSuchMethodException ex2) {
                try {
                    return (Constructor<Exception>) exceptionClass.getConstructor(String.class);
                } catch (NoSuchMethodException ex3) {
                    throw new IllegalArgumentException(
                            "Service locator exception [" + exceptionClass.getName() +
                                    "] neither has a (String, Throwable) constructor nor a (String) constructor");
                }
            }
        }
    }

    /**
     * Create a service locator exception for the given cause.
     * Only called in case of a custom service locator exception.
     * <p>The default implementation can handle all variations of
     * message and exception arguments.
     *
     * @param exceptionConstructor the constructor to use
     * @param cause                the cause of the service lookup failure
     * @return the service locator exception to throw
     * @see #setServiceLocatorExceptionClass
     */
    protected Exception createServiceLocatorException(Constructor<Exception> exceptionConstructor, BeansException cause) {
        Class<?>[] paramTypes = exceptionConstructor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (String.class == paramTypes[i]) {
                args[i] = cause.getMessage();
            } else if (paramTypes[i].isInstance(cause)) {
                args[i] = cause;
            }
        }
        return BeanUtils.instantiateClass(exceptionConstructor, args);
    }


    @Override
    public Object getObject() {
        return this.proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return this.serviceLocatorInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


    /**
     * Invocation handler that delegates service locator calls to the bean factory.
     */
    private class ServiceLocatorInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (ReflectionUtils.isEqualsMethod(method)) {
                // Only consider equal when proxies are identical.
                return (proxy == args[0]);
            } else if (ReflectionUtils.isHashCodeMethod(method)) {
                // Use hashCode of service locator proxy.
                return System.identityHashCode(proxy);
            } else if (ReflectionUtils.isToStringMethod(method)) {
                return "Service locator: " + serviceLocatorInterface;
            } else {
                return invokeServiceLocatorMethod(method, args);
            }
        }

        private Object invokeServiceLocatorMethod(Method method, Object[] args) throws Exception {
            Class<?> serviceLocatorMethodReturnType = getServiceLocatorMethodReturnType(method);
            try {
                String beanName = tryGetBeanName(args);
                if (StringUtils.hasLength(beanName)) {
                    // Service locator for a specific bean name
                    return beanFactory.getBean(beanName, serviceLocatorMethodReturnType);
                } else {
                    // Service locator for a bean type
                    return beanFactory.getBean(serviceLocatorMethodReturnType);
                }
            } catch (BeansException ex) {
                if (serviceLocatorExceptionConstructor != null) {
                    throw createServiceLocatorException(serviceLocatorExceptionConstructor, ex);
                }
                throw ex;
            }
        }

        /**
         * Check whether a service id was passed in.
         */
        private String tryGetBeanName(Object[] args) {
            String beanName = "";
            if (args != null && args.length == 1 && args[0] != null) {
                beanName = args[0].toString();
            }
            // Look for explicit serviceId-to-beanName mappings.
            if (serviceMappings != null) {
                String mappedName = serviceMappings.getProperty(beanName);
                if (mappedName != null) {
                    beanName = mappedName;
                }
            }
            return beanName;
        }

        private Class<?> getServiceLocatorMethodReturnType(Method method) throws NoSuchMethodException {
            Class<?>[] paramTypes = method.getParameterTypes();
            Method interfaceMethod = serviceLocatorInterface.getMethod(method.getName(), paramTypes);
            Class<?> serviceLocatorReturnType = interfaceMethod.getReturnType();

            // Check whether the method is a valid service locator.
            if (paramTypes.length > 1 || void.class == serviceLocatorReturnType) {
                throw new UnsupportedOperationException(
                        "May only call methods with signature '<type> xxx()' or '<type> xxx(<idtype> id)' " +
                                "on factory interface, but tried to call: " + interfaceMethod);
            }
            return serviceLocatorReturnType;
        }
    }

}
