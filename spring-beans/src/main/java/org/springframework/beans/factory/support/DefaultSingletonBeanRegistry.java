package org.springframework.beans.factory.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.core.SimpleAliasRegistry;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

//默认单例Bean注册器
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {

    //日志类
    protected final Log logger = LogFactory.getLog(getClass());

    //空对象
    protected static final Object NULL_OBJECT = new Object();

    //单例对象集合
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<String, Object>(256);

    //单例对象工厂集合
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<String, ObjectFactory<?>>(16);

    //早期单例集合
    private final Map<String, Object> earlySingletonObjects = new HashMap<String, Object>(16);

    //已注册单例集合
    private final Set<String> registeredSingletons = new LinkedHashSet<String>(256);

    //正在创建的Bean名称集合
    private final Set<String> singletonsCurrentlyInCreation = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(16));

    //未在创建的Bean名称集合
    private final Set<String> inCreationCheckExclusions = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(16));

    //被抑制的异常集合
    private Set<Exception> suppressedExceptions;

    //单例是否正在摧毁
    private boolean singletonsCurrentlyInDestruction = false;

    //一次性Bean集合
    private final Map<String, Object> disposableBeans = new LinkedHashMap<String, Object>();

    //包含关系映射
    private final Map<String, Set<String>> containedBeanMap = new ConcurrentHashMap<String, Set<String>>(16);

    //依赖关系映射(被依赖Bean，依赖Bean集合)
    private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<String, Set<String>>(64);

    //依赖关系映射(依赖Bean，被依赖Bean集合)
    private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<String, Set<String>>(64);

    //注册单例
    @Override
    public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
        Assert.notNull(beanName, "'beanName' must not be null");
        synchronized (this.singletonObjects) {
            //从缓存中获取对象
            Object oldObject = this.singletonObjects.get(beanName);
            //如果对象已存在，则抛出异常
            if (oldObject != null) {
                throw new IllegalStateException("Could not register object [" + singletonObject +
                        "] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
            }
            //若不存在，则添加单例对象
            addSingleton(beanName, singletonObject);
        }
    }

    //添加单例
    protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            //将该对象放入缓存映射
            this.singletonObjects.put(beanName, (singletonObject != null ? singletonObject : NULL_OBJECT));
            //移除对应的工厂
            this.singletonFactories.remove(beanName);
            //移除早期的单例
            this.earlySingletonObjects.remove(beanName);
            //将该Bean设置为已注册
            this.registeredSingletons.add(beanName);
        }
    }

    //添加单例工厂
    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(singletonFactory, "Singleton factory must not be null");
        synchronized (this.singletonObjects) {
            //检查是否没有包含该Bean
            if (!this.singletonObjects.containsKey(beanName)) {
                //将单例工厂放入映射
                this.singletonFactories.put(beanName, singletonFactory);
                //移除早期单例对象
                this.earlySingletonObjects.remove(beanName);
                //添加到已注册集合
                this.registeredSingletons.add(beanName);
            }
        }
    }

    //获取单例(Bean名称)
    @Override
    public Object getSingleton(String beanName) {
        return getSingleton(beanName, true);
    }

    //获取单例(Bean名称，是否提前引用)
    protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        //根据名称从缓存中获取Bean对象
        Object singletonObject = this.singletonObjects.get(beanName);
        //判断当前Bean对象是否正在被创建
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            synchronized (this.singletonObjects) {
                //获取早期单例对象
                singletonObject = this.earlySingletonObjects.get(beanName);
                if (singletonObject == null && allowEarlyReference) {
                    //获取该Bean对应的对象工厂
                    ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                    if (singletonFactory != null) {
                        //通过对象工厂创建Bean对象
                        singletonObject = singletonFactory.getObject();
                        //放置在早期单例集合中
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        //将该Bean对应的对象工厂移除
                        this.singletonFactories.remove(beanName);
                    }
                }
            }
        }
        return (singletonObject != NULL_OBJECT ? singletonObject : null);
    }

    //获取单例(Bean名称，对象工厂)
    public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(beanName, "'beanName' must not be null");
        synchronized (this.singletonObjects) {
            //根据名称从缓存中获取Bean
            Object singletonObject = this.singletonObjects.get(beanName);
            //若缓存中不存在该Bean实例，则通过对象工厂获取
            if (singletonObject == null) {
                //若发现该Bean正在被销毁中，则抛出异常
                if (this.singletonsCurrentlyInDestruction) {
                    throw new BeanCreationNotAllowedException(beanName,
                            "Singleton bean creation not allowed while singletons of this factory are in destruction " +
                                    "(Do not request a bean from a BeanFactory in a destroy method implementation!)");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
                }
                //在创建Bean之前执行处理操作
                beforeSingletonCreation(beanName);
                boolean newSingleton = false;
                boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
                if (recordSuppressedExceptions) {
                    this.suppressedExceptions = new LinkedHashSet<Exception>();
                }
                try {
                    //从传入的对象工厂中获取对象
                    singletonObject = singletonFactory.getObject();
                    //设置Bean被新创建的标志
                    newSingleton = true;
                } catch (IllegalStateException ex) {
                    singletonObject = this.singletonObjects.get(beanName);
                    if (singletonObject == null) {
                        throw ex;
                    }
                } catch (BeanCreationException ex) {
                    if (recordSuppressedExceptions) {
                        for (Exception suppressedException : this.suppressedExceptions) {
                            ex.addRelatedCause(suppressedException);
                        }
                    }
                    throw ex;
                } finally {
                    if (recordSuppressedExceptions) {
                        this.suppressedExceptions = null;
                    }
                    //在创建Bean之后执行处理操作
                    afterSingletonCreation(beanName);
                }
                //若该Bean是新创建，则添加到缓存中
                if (newSingleton) {
                    addSingleton(beanName, singletonObject);
                }
            }
            return (singletonObject != NULL_OBJECT ? singletonObject : null);
        }
    }

    //添加被抑制异常
    protected void onSuppressedException(Exception ex) {
        synchronized (this.singletonObjects) {
            if (this.suppressedExceptions != null) {
                this.suppressedExceptions.add(ex);
            }
        }
    }

    //移除单例
    protected void removeSingleton(String beanName) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.remove(beanName);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.remove(beanName);
        }
    }

    //是否包含指定单例
    @Override
    public boolean containsSingleton(String beanName) {
        return this.singletonObjects.containsKey(beanName);
    }

    //获取所有单例名称
    @Override
    public String[] getSingletonNames() {
        synchronized (this.singletonObjects) {
            return StringUtils.toStringArray(this.registeredSingletons);
        }
    }

    //获取所有单例数量
    @Override
    public int getSingletonCount() {
        synchronized (this.singletonObjects) {
            return this.registeredSingletons.size();
        }
    }

    //设置当前正在创建的Bean
    public void setCurrentlyInCreation(String beanName, boolean inCreation) {
        Assert.notNull(beanName, "Bean name must not be null");
        if (!inCreation) {
            this.inCreationCheckExclusions.add(beanName);
        } else {
            this.inCreationCheckExclusions.remove(beanName);
        }
    }

    //是否当前正在创建
    public boolean isCurrentlyInCreation(String beanName) {
        Assert.notNull(beanName, "Bean name must not be null");
        return (!this.inCreationCheckExclusions.contains(beanName) && isActuallyInCreation(beanName));
    }

    //是否实际在创建
    protected boolean isActuallyInCreation(String beanName) {
        return isSingletonCurrentlyInCreation(beanName);
    }

    //是否单例正在创建
    public boolean isSingletonCurrentlyInCreation(String beanName) {
        return this.singletonsCurrentlyInCreation.contains(beanName);
    }

    //单例创建之前执行
    protected void beforeSingletonCreation(String beanName) {
        //检测该Bean是否存在未创建Bean集合中，若存在则将该Bean添加到正创建Bean集合中
        if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.add(beanName)) {
            throw new BeanCurrentlyInCreationException(beanName);
        }
    }

    //单例创建之后执行
    protected void afterSingletonCreation(String beanName) {
        if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.remove(beanName)) {
            throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in creation");
        }
    }


    //注册一次性Bean
    public void registerDisposableBean(String beanName, DisposableBean bean) {
        synchronized (this.disposableBeans) {
            this.disposableBeans.put(beanName, bean);
        }
    }

    //注册Bean的包含关系
    public void registerContainedBean(String containedBeanName, String containingBeanName) {
        //根据包含Bean名称获取被包含Bean集合
        Set<String> containedBeans = this.containedBeanMap.get(containingBeanName);
        //若被包含Bean存在于集合中，则直接返回
        if (containedBeans != null && containedBeans.contains(containedBeanName)) {
            return;
        }

        synchronized (this.containedBeanMap) {
            //再次根据包含Bean名称获取被包含Bean集合
            containedBeans = this.containedBeanMap.get(containingBeanName);
            //如果被包含Bean集合为空
            if (containedBeans == null) {
                //新建被包含Bean集合
                containedBeans = new LinkedHashSet<String>(8);
                //建立包含Bean名称与被包含Bean集合之间映射
                this.containedBeanMap.put(containingBeanName, containedBeans);
            }
            //将被包含Bean添加到被包含Bean集合中
            containedBeans.add(containedBeanName);
        }
        //注册Bean的依赖关系
        registerDependentBean(containedBeanName, containingBeanName);
    }

    //注册Bean的依赖关系
    public void registerDependentBean(String beanName, String dependentBeanName) {
        //获取被依赖Bean的规范名称
        String canonicalName = canonicalName(beanName);
        //根据被依赖Bean获取依赖Bean集合
        Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
        //若依赖Bean集合已包含该依赖Bean，则直接返回
        if (dependentBeans != null && dependentBeans.contains(dependentBeanName)) {
            return;
        }

        synchronized (this.dependentBeanMap) {
            //再次根据被依赖Bean获取依赖Bean集合
            dependentBeans = this.dependentBeanMap.get(canonicalName);
            //如果依赖Bean集合为空
            if (dependentBeans == null) {
                //新建依赖Bean集合
                dependentBeans = new LinkedHashSet<String>(8);
                //建立被依赖Bean名称和依赖Bean集合的映射
                this.dependentBeanMap.put(canonicalName, dependentBeans);
            }
            //将依赖Bean添加到依赖Bean集合中
            dependentBeans.add(dependentBeanName);
        }

        synchronized (this.dependenciesForBeanMap) {
            //根据依赖Bean名称获取被依赖Bean集合
            Set<String> dependenciesForBean = this.dependenciesForBeanMap.get(dependentBeanName);
            //如果被依赖Bean名称为空
            if (dependenciesForBean == null) {
                //新建被依赖Bean集合
                dependenciesForBean = new LinkedHashSet<String>(8);
                //建立依赖Bean名称和被依赖Bean集合的映射
                this.dependenciesForBeanMap.put(dependentBeanName, dependenciesForBean);
            }
            //将被依赖Bean添加到被依赖Bean集合中
            dependenciesForBean.add(canonicalName);
        }
    }

    //检测是否存在依赖关系
    protected boolean isDependent(String beanName, String dependentBeanName) {
        return isDependent(beanName, dependentBeanName, null);
    }

    //检测是否存在依赖关系
    private boolean isDependent(String beanName, String dependentBeanName, Set<String> alreadySeen) {
        //如果Bean已经检测过，则返回false
        if (alreadySeen != null && alreadySeen.contains(beanName)) {
            return false;
        }
        //获取被依赖Bean名称
        String canonicalName = canonicalName(beanName);
        //根据被依赖Bean获取依赖Bean集合
        Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
        //若依赖Bean集合为空，则不存在依赖关系
        if (dependentBeans == null) {
            return false;
        }
        //若依赖Bean集合包含该Bean，则存在依赖关系
        if (dependentBeans.contains(dependentBeanName)) {
            return true;
        }
        //遍历依赖Bean集合
        for (String transitiveDependency : dependentBeans) {
            if (alreadySeen == null) {
                alreadySeen = new HashSet<String>();
            }
            //标记上一个Bean名称为已检测过
            alreadySeen.add(beanName);
            //递归进行依赖关系检测
            if (isDependent(transitiveDependency, dependentBeanName, alreadySeen)) {
                return true;
            }
        }
        return false;
    }

    //检查指定Bean是否被依赖
    protected boolean hasDependentBean(String beanName) {
        return this.dependentBeanMap.containsKey(beanName);
    }

    //获取依赖指定Bean的集合
    public String[] getDependentBeans(String beanName) {
        Set<String> dependentBeans = this.dependentBeanMap.get(beanName);
        if (dependentBeans == null) {
            return new String[0];
        }
        return StringUtils.toStringArray(dependentBeans);
    }

    //获取指定Bean依赖的集合
    public String[] getDependenciesForBean(String beanName) {
        Set<String> dependenciesForBean = this.dependenciesForBeanMap.get(beanName);
        if (dependenciesForBean == null) {
            return new String[0];
        }
        return dependenciesForBean.toArray(new String[dependenciesForBean.size()]);
    }

    //销毁所有单例
    public void destroySingletons() {
        if (logger.isDebugEnabled()) {
            logger.debug("Destroying singletons in " + this);
        }
        synchronized (this.singletonObjects) {
            this.singletonsCurrentlyInDestruction = true;
        }

        String[] disposableBeanNames;
        synchronized (this.disposableBeans) {
            disposableBeanNames = StringUtils.toStringArray(this.disposableBeans.keySet());
        }
        for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
            destroySingleton(disposableBeanNames[i]);
        }

        this.containedBeanMap.clear();
        this.dependentBeanMap.clear();
        this.dependenciesForBeanMap.clear();

        synchronized (this.singletonObjects) {
            this.singletonObjects.clear();
            this.singletonFactories.clear();
            this.earlySingletonObjects.clear();
            this.registeredSingletons.clear();
            this.singletonsCurrentlyInDestruction = false;
        }
    }

    //销毁指定单例
    public void destroySingleton(String beanName) {
        //移除指定单例
        removeSingleton(beanName);
        DisposableBean disposableBean;
        synchronized (this.disposableBeans) {
            //将指定Bean从一次性Bean集合中移除
            disposableBean = (DisposableBean) this.disposableBeans.remove(beanName);
        }
        //销毁Bean对象
        destroyBean(beanName, disposableBean);
    }

    //销毁Bean对象
    protected void destroyBean(String beanName, DisposableBean bean) {
        //移除并获取指定Bean的依赖关系
        Set<String> dependencies = this.dependentBeanMap.remove(beanName);
        if (dependencies != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved dependent beans for bean '" + beanName + "': " + dependencies);
            }
            //遍历依赖关系
            for (String dependentBeanName : dependencies) {
                //销毁所有依赖Bean
                destroySingleton(dependentBeanName);
            }
        }

        //如果一次性Bean不为空，则销毁一次性Bean
        if (bean != null) {
            try {
                bean.destroy();
            } catch (Throwable ex) {
                logger.error("Destroy method on bean with name '" + beanName + "' threw an exception", ex);
            }
        }

        //移除并获取指定Bean的包含集合
        Set<String> containedBeans = this.containedBeanMap.remove(beanName);
        if (containedBeans != null) {
            //遍历包含关系
            for (String containedBeanName : containedBeans) {
                //销毁所有包含Bean
                destroySingleton(containedBeanName);
            }
        }

        synchronized (this.dependentBeanMap) {
            for (Iterator<Map.Entry<String, Set<String>>> it = this.dependentBeanMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Set<String>> entry = it.next();
                Set<String> dependenciesToClean = entry.getValue();
                dependenciesToClean.remove(beanName);
                if (dependenciesToClean.isEmpty()) {
                    it.remove();
                }
            }
        }

        //移除指定Bean的依赖关系
        this.dependenciesForBeanMap.remove(beanName);
    }

    //获取单例互斥体
    public final Object getSingletonMutex() {
        return this.singletonObjects;
    }

}
