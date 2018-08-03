package org.springframework.beans.factory.support;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.exception.BeanCreationException;
import org.springframework.beans.exception.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.bean.factorybean.FactoryBean;
import org.springframework.beans.exception.FactoryBeanNotInitializedException;

//工厂Bean注册器助手
public abstract class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry {

    //Bean名称与Bean对象的映射集
    private final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>(16);

    //从工厂Bean中获取对象类型
    protected Class<?> getTypeForFactoryBean(final FactoryBean<?> factoryBean) {
        try {
            if (System.getSecurityManager() != null) {
                return AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
                    @Override
                    public Class<?> run() {
                        return factoryBean.getObjectType();
                    }
                }, getAccessControlContext());
            } else {
                return factoryBean.getObjectType();
            }
        } catch (Throwable ex) {
            logger.warn("FactoryBean threw exception from getObjectType, despite the contract saying " +
                    "that it should return null if the type of its object cannot be determined yet", ex);
            return null;
        }
    }

    //根据名称从缓存中获取Bean对象
    protected Object getCachedObjectForFactoryBean(String beanName) {
        Object object = this.factoryBeanObjectCache.get(beanName);
        return (object != NULL_OBJECT ? object : null);
    }

    //通过工厂Bean获取Bean对象
    protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
        //如果工厂Bean和要生成的Bean都是单例
        if (factory.isSingleton() && containsSingleton(beanName)) {
            synchronized (getSingletonMutex()) {
                //根据名称从缓存中获取Bean对象
                Object object = this.factoryBeanObjectCache.get(beanName);
                if (object == null) {
                    //通过工厂Bean来获取Bean对象
                    object = doGetObjectFromFactoryBean(factory, beanName);
                    //再次根据名称从缓存中获取Bean对象
                    Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
                    //若Bean对象已存在缓存中，则使用缓存中的Bean
                    if (alreadyThere != null) {
                        object = alreadyThere;
                    } else {
                        //如果成功获取Bean对象，并且需要进行后置处理
                        if (object != null && shouldPostProcess) {
                            try {
                                //执行后置处理操作
                                object = postProcessObjectFromFactoryBean(object, beanName);
                            } catch (Throwable ex) {
                                throw new BeanCreationException(beanName,
                                        "Post-processing of FactoryBean's singleton object failed", ex);
                            }
                        }
                        //将获得的Bean对象放入缓存中
                        this.factoryBeanObjectCache.put(beanName, (object != null ? object : NULL_OBJECT));
                    }
                }
                //返回Bean对象，若从缓存获取的是NULL_OBJECT则一直返回null
                return (object != NULL_OBJECT ? object : null);
            }
        } else {
            //通过工厂Bean来获取Bean对象
            Object object = doGetObjectFromFactoryBean(factory, beanName);
            //如果成功获取Bean对象，并且需要进行后置处理
            if (object != null && shouldPostProcess) {
                try {
                    //执行后置处理操作
                    object = postProcessObjectFromFactoryBean(object, beanName);
                } catch (Throwable ex) {
                    throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
                }
            }
            //直接返回该Bean对象
            return object;
        }
    }

    //通过工厂Bean获取Bean对象
    private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName)
            throws BeanCreationException {
        Object object;
        try {
            if (System.getSecurityManager() != null) {
                AccessControlContext acc = getAccessControlContext();
                try {
                    object = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                        @Override
                        public Object run() throws Exception {
                            return factory.getObject();
                        }
                    }, acc);
                } catch (PrivilegedActionException pae) {
                    throw pae.getException();
                }
            } else {
                object = factory.getObject();
            }
        } catch (FactoryBeanNotInitializedException ex) {
            throw new BeanCurrentlyInCreationException(beanName, ex.toString());
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
        }
        //若对象为空且该Bean正在被创建，则抛出异常
        if (object == null && isSingletonCurrentlyInCreation(beanName)) {
            throw new BeanCurrentlyInCreationException(
                    beanName, "FactoryBean which is currently in creation returned null from getObject");
        }
        return object;
    }

    //执行后置处理
    protected Object postProcessObjectFromFactoryBean(Object object, String beanName) throws BeansException {
        return object;
    }

    //将Object转为FactoryBean实例
    protected FactoryBean<?> getFactoryBean(String beanName, Object beanInstance) throws BeansException {
        if (!(beanInstance instanceof FactoryBean)) {
            throw new BeanCreationException(beanName,
                    "Bean instance of type [" + beanInstance.getClass() + "] is not a FactoryBean");
        }
        return (FactoryBean<?>) beanInstance;
    }

    //移除Bean对象
    @Override
    protected void removeSingleton(String beanName) {
        super.removeSingleton(beanName);
        this.factoryBeanObjectCache.remove(beanName);
    }

    //销毁Bean对象
    @Override
    public void destroySingletons() {
        super.destroySingletons();
        this.factoryBeanObjectCache.clear();
    }

    //获取访问控制上下文
    protected AccessControlContext getAccessControlContext() {
        return AccessController.getContext();
    }

}
