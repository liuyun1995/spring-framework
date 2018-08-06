package org.springframework.beans.factory.access;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.exception.FatalBeanException;
import org.springframework.beans.exception.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ConfigurableBeanFactory;
import org.springframework.beans.factory.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.reader.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

//单例Bean工厂定位器
public class SingletonBeanFactoryLocator implements BeanFactoryLocator {

    private static final String DEFAULT_RESOURCE_LOCATION = "classpath*:beanRefFactory.xml";

    protected static final Log logger = LogFactory.getLog(SingletonBeanFactoryLocator.class);

    private static final Map<String, BeanFactoryLocator> instances = new HashMap<String, BeanFactoryLocator>();

    //获取实例
    public static BeanFactoryLocator getInstance() throws BeansException {
        return getInstance(null);
    }

    //获取实例
    public static BeanFactoryLocator getInstance(String selector) throws BeansException {
        String resourceLocation = selector;
        if (resourceLocation == null) {
            resourceLocation = DEFAULT_RESOURCE_LOCATION;
        }

        if (!ResourcePatternUtils.isUrl(resourceLocation)) {
            resourceLocation = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resourceLocation;
        }

        synchronized (instances) {
            if (logger.isTraceEnabled()) {
                logger.trace("SingletonBeanFactoryLocator.getInstance(): instances.hashCode=" +
                        instances.hashCode() + ", instances=" + instances);
            }
            BeanFactoryLocator bfl = instances.get(resourceLocation);
            if (bfl == null) {
                bfl = new SingletonBeanFactoryLocator(resourceLocation);
                instances.put(resourceLocation, bfl);
            }
            return bfl;
        }
    }

    // We map BeanFactoryGroup objects by String keys, and by the definition object.
    private final Map<String, BeanFactoryGroup> bfgInstancesByKey = new HashMap<String, BeanFactoryGroup>();

    private final Map<BeanFactory, BeanFactoryGroup> bfgInstancesByObj = new HashMap<BeanFactory, BeanFactoryGroup>();

    private final String resourceLocation;

    protected SingletonBeanFactoryLocator(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    //使用Bean工厂
    @Override
    public BeanFactoryReference useBeanFactory(String factoryKey) throws BeansException {
        synchronized (this.bfgInstancesByKey) {
            BeanFactoryGroup bfg = this.bfgInstancesByKey.get(this.resourceLocation);

            if (bfg != null) {
                bfg.refCount++;
            } else {
                // This group definition doesn't exist, we need to try to load it.
                if (logger.isTraceEnabled()) {
                    logger.trace("Factory group with resource name [" + this.resourceLocation +
                            "] requested. Creating new instance.");
                }

                // Create the BeanFactory but don't initialize it.
                BeanFactory groupContext = createDefinition(this.resourceLocation, factoryKey);

                // Record its existence now, before instantiating any singletons.
                bfg = new BeanFactoryGroup();
                bfg.definition = groupContext;
                bfg.refCount = 1;
                this.bfgInstancesByKey.put(this.resourceLocation, bfg);
                this.bfgInstancesByObj.put(groupContext, bfg);

                // Now initialize the BeanFactory. This may cause a re-entrant invocation
                // of this method, but since we've already added the BeanFactory to our
                // mappings, the next time it will be found and simply have its
                // reference count incremented.
                try {
                    initializeDefinition(groupContext);
                } catch (BeansException ex) {
                    this.bfgInstancesByKey.remove(this.resourceLocation);
                    this.bfgInstancesByObj.remove(groupContext);
                    throw new BootstrapException("Unable to initialize group definition. " +
                            "Group resource name [" + this.resourceLocation + "], factory key [" + factoryKey + "]", ex);
                }
            }

            try {
                BeanFactory beanFactory;
                if (factoryKey != null) {
                    beanFactory = bfg.definition.getBean(factoryKey, BeanFactory.class);
                } else {
                    beanFactory = bfg.definition.getBean(BeanFactory.class);
                }
                return new CountingBeanFactoryReference(beanFactory, bfg.definition);
            } catch (BeansException ex) {
                throw new BootstrapException("Unable to return specified BeanFactory instance: factory key [" +
                        factoryKey + "], from group with resource name [" + this.resourceLocation + "]", ex);
            }

        }
    }

    //创建定义
    protected BeanFactory createDefinition(String resourceLocation, String factoryKey) {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

        try {
            Resource[] configResources = resourcePatternResolver.getResources(resourceLocation);
            if (configResources.length == 0) {
                throw new FatalBeanException("Unable to find resource for specified definition. " +
                        "Group resource name [" + this.resourceLocation + "], factory key [" + factoryKey + "]");
            }
            //加载Bean定义
            reader.loadBeanDefinitions(configResources);
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException(
                    "Error accessing bean definition resource [" + this.resourceLocation + "]", ex);
        } catch (BeanDefinitionStoreException ex) {
            throw new FatalBeanException("Unable to load group definition: " +
                    "group resource name [" + this.resourceLocation + "], factory key [" + factoryKey + "]", ex);
        }

        return factory;
    }

    //初始化定义
    protected void initializeDefinition(BeanFactory groupDef) {
        if (groupDef instanceof ConfigurableListableBeanFactory) {
            ((ConfigurableListableBeanFactory) groupDef).preInstantiateSingletons();
        }
    }

    //销毁定义
    protected void destroyDefinition(BeanFactory groupDef, String selector) {
        if (groupDef instanceof ConfigurableBeanFactory) {
            if (logger.isTraceEnabled()) {
                logger.trace("Factory group with selector '" + selector +
                        "' being released, as there are no more references to it");
            }
            ((ConfigurableBeanFactory) groupDef).destroySingletons();
        }
    }


    /**
     * We track BeanFactory instances with this class.
     */
    private static class BeanFactoryGroup {

        private BeanFactory definition;

        private int refCount = 0;
    }


    /**
     * BeanFactoryReference implementation for this locator.
     */
    private class CountingBeanFactoryReference implements BeanFactoryReference {

        private BeanFactory beanFactory;

        private BeanFactory groupContextRef;

        public CountingBeanFactoryReference(BeanFactory beanFactory, BeanFactory groupContext) {
            this.beanFactory = beanFactory;
            this.groupContextRef = groupContext;
        }

        @Override
        public BeanFactory getFactory() {
            return this.beanFactory;
        }

        // Note that it's legal to call release more than once!
        @Override
        public void release() throws FatalBeanException {
            synchronized (bfgInstancesByKey) {
                BeanFactory savedRef = this.groupContextRef;
                if (savedRef != null) {
                    this.groupContextRef = null;
                    BeanFactoryGroup bfg = bfgInstancesByObj.get(savedRef);
                    if (bfg != null) {
                        bfg.refCount--;
                        if (bfg.refCount == 0) {
                            destroyDefinition(savedRef, resourceLocation);
                            bfgInstancesByKey.remove(resourceLocation);
                            bfgInstancesByObj.remove(savedRef);
                        }
                    } else {
                        // This should be impossible.
                        logger.warn("Tried to release a SingletonBeanFactoryLocator group definition " +
                                "more times than it has actually been used. Resource name [" + resourceLocation + "]");
                    }
                }
            }
        }
    }

}
