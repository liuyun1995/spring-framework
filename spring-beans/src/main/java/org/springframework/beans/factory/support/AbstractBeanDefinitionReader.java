package org.springframework.beans.factory.support;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;

//抽象Bean定义阅读器
public abstract class AbstractBeanDefinitionReader implements EnvironmentCapable, BeanDefinitionReader {

    protected final Log logger = LogFactory.getLog(getClass());                     //日志类
    private final BeanDefinitionRegistry registry;                                  //Bean定义注册器
    private ResourceLoader resourceLoader;                                          //资源加载器
    private ClassLoader beanClassLoader;                                            //Bean加载器
    private Environment environment;                                                //运行环境
    private BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();   //Bean名称生成器

    //构造器
    protected AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
        Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
        this.registry = registry;
        //如果注册器实现了ResourceLoader接口
        if (this.registry instanceof ResourceLoader) {
            this.resourceLoader = (ResourceLoader) this.registry;
        } else {
            //否则，默认使用PathMatchingResourcePatternResolver
            this.resourceLoader = new PathMatchingResourcePatternResolver();
        }

        //如果注册器实现了EnvironmentCapable接口
        if (this.registry instanceof EnvironmentCapable) {
            this.environment = ((EnvironmentCapable) this.registry).getEnvironment();
        } else {
            //否则，默认使用StandardEnvironment
            this.environment = new StandardEnvironment();
        }
    }

    //获取Bean工厂
    public final BeanDefinitionRegistry getBeanFactory() {
        return this.registry;
    }

    //获取Bean定义注册器
    @Override
    public final BeanDefinitionRegistry getRegistry() {
        return this.registry;
    }

    //设置资源加载器
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    //获取资源加载器
    @Override
    public ResourceLoader getResourceLoader() {
        return this.resourceLoader;
    }

    //设置Bean加载器
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;
    }

    //获取Bean加载器
    @Override
    public ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

    //设置环境信息
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    //获取环境信息
    @Override
    public Environment getEnvironment() {
        return this.environment;
    }

    //设置Bean名称生成器
    public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
        this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : new DefaultBeanNameGenerator());
    }

    //获取Bean名称生成器
    @Override
    public BeanNameGenerator getBeanNameGenerator() {
        return this.beanNameGenerator;
    }

    //加载Bean定义(Resource实例数组)
    @Override
    public int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException {
        Assert.notNull(resources, "Resource array must not be null");
        int counter = 0;
        //遍历资源集合，统计加载Bean定义的数量
        for (Resource resource : resources) {
            counter += loadBeanDefinitions(resource);
        }
        return counter;
    }

    //加载Bean定义(单个资源路径)
    @Override
    public int loadBeanDefinitions(String location) throws BeanDefinitionStoreException {
        return loadBeanDefinitions(location, null);
    }

    //加载Bean定义(资源路径数组)
    @Override
    public int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException {
        Assert.notNull(locations, "Location array must not be null");
        int counter = 0;
        for (String location : locations) {
            counter += loadBeanDefinitions(location);
        }
        return counter;
    }

    //加载Bean定义(资源路径，已加载集合)
    public int loadBeanDefinitions(String location, Set<Resource> actualResources) throws BeanDefinitionStoreException {
        //获取资源文件加载器
        ResourceLoader resourceLoader = getResourceLoader();
        if (resourceLoader == null) {
            throw new BeanDefinitionStoreException(
                    "Cannot import bean definitions from location [" + location + "]: no ResourceLoader available");
        }

        //如果资源加载器实现了ResourcePatternResolver
        if (resourceLoader instanceof ResourcePatternResolver) {
            try {
                //获取对应路径的全部资源
                Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);

                int loadCount = loadBeanDefinitions(resources);
                if (actualResources != null) {
                    for (Resource resource : resources) {
                        actualResources.add(resource);
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Loaded " + loadCount + " bean definitions from location pattern [" + location + "]");
                }
                return loadCount;
            } catch (IOException ex) {
                throw new BeanDefinitionStoreException(
                        "Could not resolve bean definition resource pattern [" + location + "]", ex);
            }
        } else {
            //否则，只能通过绝对路径加载单个资源
            Resource resource = resourceLoader.getResource(location);
            int loadCount = loadBeanDefinitions(resource);
            if (actualResources != null) {
                actualResources.add(resource);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded " + loadCount + " bean definitions from location [" + location + "]");
            }
            return loadCount;
        }
    }

}
