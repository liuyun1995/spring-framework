package org.springframework.beans.factory.support;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;

//工厂Bean注册器助手
public abstract class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry {

    //工厂Bean对象缓存
    private final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>(16);


    //从工厂Bean中获取对象类型
    protected Class<?> getTypeForFactoryBean(final FactoryBean<?> factoryBean) {
        try {
            if (System.getSecurityManager() != null) {
                return AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
                    @Override
                    public Class<?> run() {
                        //获取对象类型
                        return factoryBean.getObjectType();
                    }
                }, getAccessControlContext());
            } else {
                //获取对象类型
                return factoryBean.getObjectType();
            }
        } catch (Throwable ex) {
            logger.warn("FactoryBean threw exception from getObjectType, despite the contract saying " +
                    "that it should return null if the type of its object cannot be determined yet", ex);
            return null;
        }
    }

    //根据Bean名称获取对应工厂Bean
    protected Object getCachedObjectForFactoryBean(String beanName) {
        //根据Bean名称获取对应工厂Bean
        Object object = this.factoryBeanObjectCache.get(beanName);
        //如果不为空则返回工厂Bean对象
        return (object != NULL_OBJECT ? object : null);
    }

    //获取工厂Bean对象
    protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
        //如果工厂Bean是单例
        if (factory.isSingleton() && containsSingleton(beanName)) {
            synchronized (getSingletonMutex()) {
                //根据Bean名称获取对应工厂Bean
                Object object = this.factoryBeanObjectCache.get(beanName);
                //若缓存中未找到对应工厂Bean
                if (object == null) {
                    //通过工厂Bean来获取对象
                    object = doGetObjectFromFactoryBean(factory, beanName);
                    //再次从缓存中获取工厂Bean
                    Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
                    //如果工厂Bean已在缓存中
                    if (alreadyThere != null) {
                        //使用缓存中的工厂Bean
                        object = alreadyThere;
                    } else {
                        if (object != null && shouldPostProcess) {
                            try {
                                //执行后置处理操作
                                object = postProcessObjectFromFactoryBean(object, beanName);
                            } catch (Throwable ex) {
                                throw new BeanCreationException(beanName,
                                        "Post-processing of FactoryBean's singleton object failed", ex);
                            }
                        }
                        //建立生成Bean名称与工厂Bean的映射
                        this.factoryBeanObjectCache.put(beanName, (object != null ? object : NULL_OBJECT));
                    }
                }
                //返回工厂Bean对象
                return (object != NULL_OBJECT ? object : null);
            }
        } else {
            //通过工厂Bean获取对象
            Object object = doGetObjectFromFactoryBean(factory, beanName);
            //如果对象不为空并且需要进行后置处理
            if (object != null && shouldPostProcess) {
                try {
                    //执行后置处理操作
                    object = postProcessObjectFromFactoryBean(object, beanName);
                } catch (Throwable ex) {
                    throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
                }
            }
            //返回工厂Bean对象
            return object;
        }
    }

    //获取工厂Bean对象(通过工厂Bean)
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
                            //通过工厂Bean来获取对象
                            return factory.getObject();
                        }
                    }, acc);
                } catch (PrivilegedActionException pae) {
                    throw pae.getException();
                }
            } else {
                //通过工厂Bean来获取对象
                object = factory.getObject();
            }
        } catch (FactoryBeanNotInitializedException ex) {
            throw new BeanCurrentlyInCreationException(beanName, ex.toString());
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
        }
        //如果对象为空并且该Bean正在被创建，则抛出异常
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

    //获取工厂Bean实例
    protected FactoryBean<?> getFactoryBean(String beanName, Object beanInstance) throws BeansException {
        if (!(beanInstance instanceof FactoryBean)) {
            throw new BeanCreationException(beanName,
                    "Bean instance of type [" + beanInstance.getClass() + "] is not a FactoryBean");
        }
        return (FactoryBean<?>) beanInstance;
    }

    //移除单例
    @Override
    protected void removeSingleton(String beanName) {
        super.removeSingleton(beanName);
        this.factoryBeanObjectCache.remove(beanName);
    }

    //销毁单例
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
