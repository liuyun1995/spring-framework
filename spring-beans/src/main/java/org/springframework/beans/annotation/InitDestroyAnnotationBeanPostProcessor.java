package org.springframework.beans.annotation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.exception.BeansException;
import org.springframework.beans.exception.BeanCreationException;
import org.springframework.beans.support.processor.DestructionAwareBeanPostProcessor;
import org.springframework.beans.support.processor.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.bean.definition.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

@SuppressWarnings("serial")
public class InitDestroyAnnotationBeanPostProcessor
        implements DestructionAwareBeanPostProcessor, MergedBeanDefinitionPostProcessor, PriorityOrdered, Serializable {

    protected transient Log logger = LogFactory.getLog(getClass());
    private Class<? extends Annotation> initAnnotationType;
    private Class<? extends Annotation> destroyAnnotationType;
    private int order = Ordered.LOWEST_PRECEDENCE;
    private transient final Map<Class<?>, LifecycleMetadata> lifecycleMetadataCache = new ConcurrentHashMap<Class<?>, LifecycleMetadata>(256);


    public void setInitAnnotationType(Class<? extends Annotation> initAnnotationType) {
        this.initAnnotationType = initAnnotationType;
    }

    public void setDestroyAnnotationType(Class<? extends Annotation> destroyAnnotationType) {
        this.destroyAnnotationType = destroyAnnotationType;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (beanType != null) {
            LifecycleMetadata metadata = findLifecycleMetadata(beanType);
            metadata.checkConfigMembers(beanDefinition);
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        LifecycleMetadata metadata = findLifecycleMetadata(bean.getClass());
        try {
            metadata.invokeInitMethods(bean, beanName);
        } catch (InvocationTargetException ex) {
            throw new BeanCreationException(beanName, "Invocation of init method failed", ex.getTargetException());
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Failed to invoke init method", ex);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        LifecycleMetadata metadata = findLifecycleMetadata(bean.getClass());
        try {
            metadata.invokeDestroyMethods(bean, beanName);
        } catch (InvocationTargetException ex) {
            String msg = "Invocation of destroy method failed on bean with name '" + beanName + "'";
            if (logger.isDebugEnabled()) {
                logger.warn(msg, ex.getTargetException());
            } else {
                logger.warn(msg + ": " + ex.getTargetException());
            }
        } catch (Throwable ex) {
            logger.error("Failed to invoke destroy method on bean with name '" + beanName + "'", ex);
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return findLifecycleMetadata(bean.getClass()).hasDestroyMethods();
    }


    private LifecycleMetadata findLifecycleMetadata(Class<?> clazz) {
        if (this.lifecycleMetadataCache == null) {
            // Happens after deserialization, during destruction...
            return buildLifecycleMetadata(clazz);
        }
        // Quick check on the concurrent map first, with minimal locking.
        LifecycleMetadata metadata = this.lifecycleMetadataCache.get(clazz);
        if (metadata == null) {
            synchronized (this.lifecycleMetadataCache) {
                metadata = this.lifecycleMetadataCache.get(clazz);
                if (metadata == null) {
                    metadata = buildLifecycleMetadata(clazz);
                    this.lifecycleMetadataCache.put(clazz, metadata);
                }
                return metadata;
            }
        }
        return metadata;
    }

    //构建生命周期元数据
    private LifecycleMetadata buildLifecycleMetadata(final Class<?> clazz) {
        final boolean debug = logger.isDebugEnabled();
        LinkedList<LifecycleElement> initMethods = new LinkedList<LifecycleElement>();     //初始化方法集合
        LinkedList<LifecycleElement> destroyMethods = new LinkedList<LifecycleElement>();  //销毁方法集合
        Class<?> targetClass = clazz;

        do {
            final LinkedList<LifecycleElement> currInitMethods = new LinkedList<LifecycleElement>();
            final LinkedList<LifecycleElement> currDestroyMethods = new LinkedList<LifecycleElement>();

            ReflectionUtils.doWithLocalMethods(targetClass, new ReflectionUtils.MethodCallback() {
                @Override
                public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                    if (initAnnotationType != null) {
                        if (method.getAnnotation(initAnnotationType) != null) {
                            LifecycleElement element = new LifecycleElement(method);
                            currInitMethods.add(element);
                            if (debug) {
                                logger.debug("Found init method on class [" + clazz.getName() + "]: " + method);
                            }
                        }
                    }
                    if (destroyAnnotationType != null) {
                        if (method.getAnnotation(destroyAnnotationType) != null) {
                            currDestroyMethods.add(new LifecycleElement(method));
                            if (debug) {
                                logger.debug("Found destroy method on class [" + clazz.getName() + "]: " + method);
                            }
                        }
                    }
                }
            });

            initMethods.addAll(0, currInitMethods);
            destroyMethods.addAll(currDestroyMethods);
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);

        return new LifecycleMetadata(clazz, initMethods, destroyMethods);
    }


    //---------------------------------------------------------------------
    // Serialization support
    //---------------------------------------------------------------------

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // Rely on default serialization; just initialize state after deserialization.
        ois.defaultReadObject();

        // Initialize transient fields.
        this.logger = LogFactory.getLog(getClass());
    }

    //生命周期元数据
    private class LifecycleMetadata {

        private final Class<?> targetClass;                              //目标类型
        private final Collection<LifecycleElement> initMethods;          //初始化方法集合
        private final Collection<LifecycleElement> destroyMethods;       //销毁方法集合
        private volatile Set<LifecycleElement> checkedInitMethods;       //受检查的初始方法
        private volatile Set<LifecycleElement> checkedDestroyMethods;    //受检查的销毁方法

        //构造器
        public LifecycleMetadata(Class<?> targetClass, Collection<LifecycleElement> initMethods,
                                 Collection<LifecycleElement> destroyMethods) {
            this.targetClass = targetClass;
            this.initMethods = initMethods;
            this.destroyMethods = destroyMethods;
        }

        //检查配置成员
        public void checkConfigMembers(RootBeanDefinition beanDefinition) {
            Set<LifecycleElement> checkedInitMethods = new LinkedHashSet<LifecycleElement>(this.initMethods.size());
            //遍历初始化方法
            for (LifecycleElement element : this.initMethods) {
                //获取方法标识符
                String methodIdentifier = element.getIdentifier();
                //如果不是额外初始化方法
                if (!beanDefinition.isExternallyManagedInitMethod(methodIdentifier)) {
                    //注册额外初始化方法
                    beanDefinition.registerExternallyManagedInitMethod(methodIdentifier);
                    //将该方法标记为检查
                    checkedInitMethods.add(element);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Registered init method on class [" + this.targetClass.getName() + "]: " + element);
                    }
                }
            }

            Set<LifecycleElement> checkedDestroyMethods = new LinkedHashSet<LifecycleElement>(this.destroyMethods.size());
            //遍历销毁方法
            for (LifecycleElement element : this.destroyMethods) {
                String methodIdentifier = element.getIdentifier();
                if (!beanDefinition.isExternallyManagedDestroyMethod(methodIdentifier)) {
                    beanDefinition.registerExternallyManagedDestroyMethod(methodIdentifier);
                    checkedDestroyMethods.add(element);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Registered destroy method on class [" + this.targetClass.getName() + "]: " + element);
                    }
                }
            }
            this.checkedInitMethods = checkedInitMethods;
            this.checkedDestroyMethods = checkedDestroyMethods;
        }

        //调用初始化方法
        public void invokeInitMethods(Object target, String beanName) throws Throwable {
            Collection<LifecycleElement> initMethodsToIterate =
                    (this.checkedInitMethods != null ? this.checkedInitMethods : this.initMethods);
            if (!initMethodsToIterate.isEmpty()) {
                boolean debug = logger.isDebugEnabled();
                for (LifecycleElement element : initMethodsToIterate) {
                    if (debug) {
                        logger.debug("Invoking init method on bean '" + beanName + "': " + element.getMethod());
                    }
                    element.invoke(target);
                }
            }
        }

        //调用销毁方法
        public void invokeDestroyMethods(Object target, String beanName) throws Throwable {
            Collection<LifecycleElement> destroyMethodsToUse =
                    (this.checkedDestroyMethods != null ? this.checkedDestroyMethods : this.destroyMethods);
            if (!destroyMethodsToUse.isEmpty()) {
                boolean debug = logger.isDebugEnabled();
                for (LifecycleElement element : destroyMethodsToUse) {
                    if (debug) {
                        logger.debug("Invoking destroy method on bean '" + beanName + "': " + element.getMethod());
                    }
                    element.invoke(target);
                }
            }
        }

        public boolean hasDestroyMethods() {
            Collection<LifecycleElement> destroyMethodsToUse =
                    (this.checkedDestroyMethods != null ? this.checkedDestroyMethods : this.destroyMethods);
            return !destroyMethodsToUse.isEmpty();
        }
    }

    //生命周期元素
    private static class LifecycleElement {

        private final Method method;       //方法
        private final String identifier;   //方法标识符

        //构造器
        public LifecycleElement(Method method) {
            if (method.getParameterTypes().length != 0) {
                throw new IllegalStateException("Lifecycle method annotation requires a no-arg method: " + method);
            }
            this.method = method;
            this.identifier = (Modifier.isPrivate(method.getModifiers()) ? ClassUtils.getQualifiedMethodName(method) : method.getName());
        }

        //获取方法
        public Method getMethod() {
            return this.method;
        }

        //获取标识符
        public String getIdentifier() {
            return this.identifier;
        }

        //调用方法
        public void invoke(Object target) throws Throwable {
            ReflectionUtils.makeAccessible(this.method);
            this.method.invoke(target, (Object[]) null);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof LifecycleElement)) {
                return false;
            }
            LifecycleElement otherElement = (LifecycleElement) other;
            return (this.identifier.equals(otherElement.identifier));
        }

        @Override
        public int hashCode() {
            return this.identifier.hashCode();
        }
    }

}
