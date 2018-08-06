package org.springframework.beans.factory.support;

import java.io.Closeable;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.bean.DisposableBean;
import org.springframework.beans.factory.support.processor.BeanPostProcessor;
import org.springframework.beans.factory.support.processor.DestructionAwareBeanPostProcessor;
import org.springframework.beans.bean.definition.AbstractBeanDefinition;
import org.springframework.beans.bean.definition.BeanDefinitionValidationException;
import org.springframework.beans.bean.definition.RootBeanDefinition;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

//一次性Bean适配器
@SuppressWarnings("serial")
class DisposableBeanAdapter implements DisposableBean, Runnable, Serializable {

    private static final Log logger = LogFactory.getLog(DisposableBeanAdapter.class);   //日志类
    private static final String CLOSE_METHOD_NAME = "close";                            //close方法名
    private static final String SHUTDOWN_METHOD_NAME = "shutdown";                      //shutdown方法名
    private final Object bean;                                                          //Bean实例
    private final String beanName;                                                      //Bean名称
    private final boolean invokeDisposableBean;                                         //是否调用一次性Bean
    private final boolean nonPublicAccessAllowed;                                       //是否有非公共访问权限
    private final AccessControlContext acc;                                             //安全控制上下文
    private String destroyMethodName;                                                   //销毁方法名称
    private transient Method destroyMethod;                                             //销毁方法
    private List<DestructionAwareBeanPostProcessor> beanPostProcessors;                 //Bean后置处理器
    private static Class<?> closeableInterface;                                         //可以关闭的接口类

    static {
        try {
            closeableInterface = ClassUtils.forName("java.lang.AutoCloseable",
                    DisposableBeanAdapter.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            closeableInterface = Closeable.class;
        }
    }

    //构造器1
    public DisposableBeanAdapter(Object bean, String beanName, RootBeanDefinition beanDefinition,
                                 List<BeanPostProcessor> postProcessors, AccessControlContext acc) {
        Assert.notNull(bean, "Disposable bean must not be null");
        this.bean = bean;
        this.beanName = beanName;
        this.invokeDisposableBean = (this.bean instanceof DisposableBean && !beanDefinition.isExternallyManagedDestroyMethod("destroy"));
        this.nonPublicAccessAllowed = beanDefinition.isNonPublicAccessAllowed();
        this.acc = acc;
        String destroyMethodName = inferDestroyMethodIfNecessary(bean, beanDefinition);
        if (destroyMethodName != null && !(this.invokeDisposableBean && "destroy".equals(destroyMethodName)) &&
                !beanDefinition.isExternallyManagedDestroyMethod(destroyMethodName)) {
            this.destroyMethodName = destroyMethodName;
            this.destroyMethod = determineDestroyMethod();
            if (this.destroyMethod == null) {
                if (beanDefinition.isEnforceDestroyMethod()) {
                    throw new BeanDefinitionValidationException("Couldn't find a destroy method named '" +
                            destroyMethodName + "' on bean with name '" + beanName + "'");
                }
            } else {
                Class<?>[] paramTypes = this.destroyMethod.getParameterTypes();
                if (paramTypes.length > 1) {
                    throw new BeanDefinitionValidationException("Method '" + destroyMethodName + "' of bean '" +
                            beanName + "' has more than one parameter - not supported as destroy method");
                } else if (paramTypes.length == 1 && boolean.class != paramTypes[0]) {
                    throw new BeanDefinitionValidationException("Method '" + destroyMethodName + "' of bean '" +
                            beanName + "' has a non-boolean parameter - not supported as destroy method");
                }
            }
        }
        this.beanPostProcessors = filterPostProcessors(postProcessors, bean);
    }

    //构造器2
    public DisposableBeanAdapter(Object bean, List<BeanPostProcessor> postProcessors, AccessControlContext acc) {
        Assert.notNull(bean, "Disposable bean must not be null");
        this.bean = bean;
        this.beanName = null;
        this.invokeDisposableBean = (this.bean instanceof DisposableBean);
        this.nonPublicAccessAllowed = true;
        this.acc = acc;
        this.beanPostProcessors = filterPostProcessors(postProcessors, bean);
    }

    //构造器3
    private DisposableBeanAdapter(Object bean, String beanName, boolean invokeDisposableBean,
                                  boolean nonPublicAccessAllowed, String destroyMethodName,
                                  List<DestructionAwareBeanPostProcessor> postProcessors) {
        this.bean = bean;
        this.beanName = beanName;
        this.invokeDisposableBean = invokeDisposableBean;
        this.nonPublicAccessAllowed = nonPublicAccessAllowed;
        this.acc = null;
        this.destroyMethodName = destroyMethodName;
        this.beanPostProcessors = postProcessors;
    }

    //推断销毁方法
    private String inferDestroyMethodIfNecessary(Object bean, RootBeanDefinition beanDefinition) {
        //获取销毁方法名称
        String destroyMethodName = beanDefinition.getDestroyMethodName();
        //销毁方法名为"(inferred)"，或者销毁方法名为空并且该Bean实现了可关闭接口
        if (AbstractBeanDefinition.INFER_METHOD.equals(destroyMethodName) ||
                (destroyMethodName == null && closeableInterface.isInstance(bean))) {
            //如果Bean不是一次性对象
            if (!(bean instanceof DisposableBean)) {
                try {
                    return bean.getClass().getMethod(CLOSE_METHOD_NAME).getName();
                } catch (NoSuchMethodException ex) {
                    try {
                        return bean.getClass().getMethod(SHUTDOWN_METHOD_NAME).getName();
                    } catch (NoSuchMethodException ex2) {
                        // no candidate destroy method found
                    }
                }
            }
            return null;
        }
        return (StringUtils.hasLength(destroyMethodName) ? destroyMethodName : null);
    }

    //后置处理器过滤器
    private List<DestructionAwareBeanPostProcessor> filterPostProcessors(List<BeanPostProcessor> processors, Object bean) {
        List<DestructionAwareBeanPostProcessor> filteredPostProcessors = null;
        if (!CollectionUtils.isEmpty(processors)) {
            filteredPostProcessors = new ArrayList<DestructionAwareBeanPostProcessor>(processors.size());
            for (BeanPostProcessor processor : processors) {
                if (processor instanceof DestructionAwareBeanPostProcessor) {
                    DestructionAwareBeanPostProcessor dabpp = (DestructionAwareBeanPostProcessor) processor;
                    try {
                        if (dabpp.requiresDestruction(bean)) {
                            filteredPostProcessors.add(dabpp);
                        }
                    } catch (AbstractMethodError err) {
                        filteredPostProcessors.add(dabpp);
                    }
                }
            }
        }
        return filteredPostProcessors;
    }

    //运行方法
    @Override
    public void run() {
        destroy();
    }

    //销毁方法
    @Override
    public void destroy() {
        if (!CollectionUtils.isEmpty(this.beanPostProcessors)) {
            for (DestructionAwareBeanPostProcessor processor : this.beanPostProcessors) {
                processor.postProcessBeforeDestruction(this.bean, this.beanName);
            }
        }

        if (this.invokeDisposableBean) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invoking destroy() on bean with name '" + this.beanName + "'");
            }
            try {
                if (System.getSecurityManager() != null) {
                    AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                        @Override
                        public Object run() throws Exception {
                            ((DisposableBean) bean).destroy();
                            return null;
                        }
                    }, acc);
                } else {
                    ((DisposableBean) bean).destroy();
                }
            } catch (Throwable ex) {
                String msg = "Invocation of destroy method failed on bean with name '" + this.beanName + "'";
                if (logger.isDebugEnabled()) {
                    logger.warn(msg, ex);
                } else {
                    logger.warn(msg + ": " + ex);
                }
            }
        }

        if (this.destroyMethod != null) {
            invokeCustomDestroyMethod(this.destroyMethod);
        } else if (this.destroyMethodName != null) {
            Method methodToCall = determineDestroyMethod();
            if (methodToCall != null) {
                invokeCustomDestroyMethod(methodToCall);
            }
        }
    }

    //确定销毁方法
    private Method determineDestroyMethod() {
        try {
            if (System.getSecurityManager() != null) {
                return AccessController.doPrivileged(new PrivilegedAction<Method>() {
                    @Override
                    public Method run() {
                        return findDestroyMethod();
                    }
                });
            } else {
                return findDestroyMethod();
            }
        } catch (IllegalArgumentException ex) {
            throw new BeanDefinitionValidationException("Could not find unique destroy method on bean with name '" +
                    this.beanName + ": " + ex.getMessage());
        }
    }

    //寻找销毁方法
    private Method findDestroyMethod() {
        return (this.nonPublicAccessAllowed ?
                BeanUtils.findMethodWithMinimalParameters(this.bean.getClass(), this.destroyMethodName) :
                BeanUtils.findMethodWithMinimalParameters(this.bean.getClass().getMethods(), this.destroyMethodName));
    }

    //调用外部销毁方法
    private void invokeCustomDestroyMethod(final Method destroyMethod) {
        Class<?>[] paramTypes = destroyMethod.getParameterTypes();
        final Object[] args = new Object[paramTypes.length];
        if (paramTypes.length == 1) {
            args[0] = Boolean.TRUE;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking destroy method '" + this.destroyMethodName +
                    "' on bean with name '" + this.beanName + "'");
        }
        try {
            if (System.getSecurityManager() != null) {
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    @Override
                    public Object run() {
                        ReflectionUtils.makeAccessible(destroyMethod);
                        return null;
                    }
                });
                try {
                    AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                        @Override
                        public Object run() throws Exception {
                            destroyMethod.invoke(bean, args);
                            return null;
                        }
                    }, acc);
                } catch (PrivilegedActionException pax) {
                    throw (InvocationTargetException) pax.getException();
                }
            } else {
                ReflectionUtils.makeAccessible(destroyMethod);
                destroyMethod.invoke(bean, args);
            }
        } catch (InvocationTargetException ex) {
            String msg = "Invocation of destroy method '" + this.destroyMethodName +
                    "' failed on bean with name '" + this.beanName + "'";
            if (logger.isDebugEnabled()) {
                logger.warn(msg, ex.getTargetException());
            } else {
                logger.warn(msg + ": " + ex.getTargetException());
            }
        } catch (Throwable ex) {
            logger.error("Couldn't invoke destroy method '" + this.destroyMethodName +
                    "' on bean with name '" + this.beanName + "'", ex);
        }
    }


    /**
     * Serializes a copy of the state of this class,
     * filtering out non-serializable BeanPostProcessors.
     */
    protected Object writeReplace() {
        List<DestructionAwareBeanPostProcessor> serializablePostProcessors = null;
        if (this.beanPostProcessors != null) {
            serializablePostProcessors = new ArrayList<DestructionAwareBeanPostProcessor>();
            for (DestructionAwareBeanPostProcessor postProcessor : this.beanPostProcessors) {
                if (postProcessor instanceof Serializable) {
                    serializablePostProcessors.add(postProcessor);
                }
            }
        }
        return new DisposableBeanAdapter(this.bean, this.beanName, this.invokeDisposableBean,
                this.nonPublicAccessAllowed, this.destroyMethodName, serializablePostProcessors);
    }


    //是否存在销毁方法
    public static boolean hasDestroyMethod(Object bean, RootBeanDefinition beanDefinition) {
        if (bean instanceof DisposableBean || closeableInterface.isInstance(bean)) {
            return true;
        }
        String destroyMethodName = beanDefinition.getDestroyMethodName();
        if (AbstractBeanDefinition.INFER_METHOD.equals(destroyMethodName)) {
            return (ClassUtils.hasMethod(bean.getClass(), CLOSE_METHOD_NAME) ||
                    ClassUtils.hasMethod(bean.getClass(), SHUTDOWN_METHOD_NAME));
        }
        return StringUtils.hasLength(destroyMethodName);
    }

    //是否有可用的加工器
    public static boolean hasApplicableProcessors(Object bean, List<BeanPostProcessor> postProcessors) {
        if (!CollectionUtils.isEmpty(postProcessors)) {
            for (BeanPostProcessor processor : postProcessors) {
                if (processor instanceof DestructionAwareBeanPostProcessor) {
                    DestructionAwareBeanPostProcessor dabpp = (DestructionAwareBeanPostProcessor) processor;
                    try {
                        if (dabpp.requiresDestruction(bean)) {
                            return true;
                        }
                    } catch (AbstractMethodError err) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
