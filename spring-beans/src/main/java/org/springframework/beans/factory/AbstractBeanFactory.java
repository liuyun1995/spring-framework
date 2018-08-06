package org.springframework.beans.factory;

import java.beans.PropertyEditor;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.bean.BeanUtils;
import org.springframework.beans.bean.BeanWrapper;
import org.springframework.beans.bean.registry.ConfigurableBeanFactory;
import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.support.DisposableBeanAdapter;
import org.springframework.beans.factory.support.FactoryBeanRegistrySupport;
import org.springframework.beans.bean.definition.RootBeanDefinition;
import org.springframework.beans.factory.support.SecurityContextProvider;
import org.springframework.beans.property.PropertyEditorRegistrar;
import org.springframework.beans.property.PropertyEditorRegistry;
import org.springframework.beans.property.PropertyEditorRegistrySupport;
import org.springframework.beans.property.type.SimpleTypeConverter;
import org.springframework.beans.property.type.TypeConverter;
import org.springframework.beans.exception.TypeMismatchException;
import org.springframework.beans.exception.BeanCreationException;
import org.springframework.beans.exception.BeanCurrentlyInCreationException;
import org.springframework.beans.exception.BeanDefinitionStoreException;
import org.springframework.beans.exception.BeanIsAbstractException;
import org.springframework.beans.exception.BeanIsNotAFactoryException;
import org.springframework.beans.exception.BeanNotOfRequiredTypeException;
import org.springframework.beans.exception.CannotLoadBeanClassException;
import org.springframework.beans.bean.factorybean.FactoryBean;
import org.springframework.beans.exception.NoSuchBeanDefinitionException;
import org.springframework.beans.bean.factorybean.SmartFactoryBean;
import org.springframework.beans.bean.definition.BeanDefinition;
import org.springframework.beans.bean.definition.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.support.processor.BeanPostProcessor;
import org.springframework.beans.factory.support.processor.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.support.processor.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.Scope;
import org.springframework.core.DecoratingClassLoader;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

//抽象的Bean工厂
public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {

    //父类Bean工厂
    private BeanFactory parentBeanFactory;

    //类加载器
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    //临时类加载器
    private ClassLoader tempClassLoader;

    //是否缓存Bean的元信息
    private boolean cacheBeanMetadata = true;

    //Bean表达式转换器
    private BeanExpressionResolver beanExpressionResolver;

    //转换服务
    private ConversionService conversionService;

    //属性编辑器注册器
    private final Set<PropertyEditorRegistrar> propertyEditorRegistrars = new LinkedHashSet<PropertyEditorRegistrar>(4);

    //外部编辑器
    private final Map<Class<?>, Class<? extends PropertyEditor>> customEditors = new HashMap<Class<?>, Class<? extends PropertyEditor>>(4);

    //类型转换器
    private TypeConverter typeConverter;

    //字符串解析器列表
    private final List<StringValueResolver> embeddedValueResolvers = new LinkedList<StringValueResolver>();

    //Bean加工器列表
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<BeanPostProcessor>();

    //是否实例化后置处理器
    private boolean hasInstantiationAwareBeanPostProcessors;

    //是否已销毁后置处理器
    private boolean hasDestructionAwareBeanPostProcessors;

    //范围映射集合
    private final Map<String, Scope> scopes = new LinkedHashMap<String, Scope>(8);

    //安全上下文提供者
    private SecurityContextProvider securityContextProvider;

    //合并的Bean定义
    private final Map<String, RootBeanDefinition> mergedBeanDefinitions = new ConcurrentHashMap<String, RootBeanDefinition>(256);

    //已被创建的Bean名称
    private final Set<String> alreadyCreated = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(256));

    //正在创建的Bean名称
    private final ThreadLocal<Object> prototypesCurrentlyInCreation = new NamedThreadLocal<Object>("Prototype beans currently in creation");

    //构造器1
    public AbstractBeanFactory() {}

    //构造器2
    public AbstractBeanFactory(BeanFactory parentBeanFactory) {
        this.parentBeanFactory = parentBeanFactory;
    }

    // ---------------------------------------------------------------------
    // 实现BeanFactory接口方法
    // ---------------------------------------------------------------------

    //获取Bean实例(根据名称)
    @Override
    public Object getBean(String name) throws BeansException {
        return doGetBean(name, null, null, false);
    }

    //获取Bean实例(根据名称, 类型)
    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return doGetBean(name, requiredType, null, false);
    }

    //获取Bean实例(根据名称, 参数)
    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        return doGetBean(name, null, args, false);
    }

    //获取Bean实例(根据名称, 类型, 参数)
    public <T> T getBean(String name, Class<T> requiredType, Object... args) throws BeansException {
        return doGetBean(name, requiredType, args, false);
    }

    //核心获取Bean方法
    @SuppressWarnings("unchecked")
    protected <T> T doGetBean(final String name, final Class<T> requiredType, final Object[] args,
                              boolean typeCheckOnly) throws BeansException {
        //提取不带"&"前缀的Bean名称
        final String beanName = transformedBeanName(name);
        Object bean;

        //根据名称获取对应Bean实例
        Object sharedInstance = getSingleton(beanName);
        //若获得的Bean不为空，且传入的构造参数为空
        if (sharedInstance != null && args == null) {
            if (logger.isDebugEnabled()) {
                if (isSingletonCurrentlyInCreation(beanName)) {
                    logger.debug("Returning eagerly cached instance of singleton bean '" + beanName
                            + "' that is not fully initialized yet - a consequence of a circular reference");
                } else {
                    logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
                }
            }
            //获取Bean对象，可通过工厂Bean来创建对象
            bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
        } else {
            //如果当前创建的Bean为原型，则抛出异常
            if (isPrototypeCurrentlyInCreation(beanName)) {
                throw new BeanCurrentlyInCreationException(beanName);
            }

            //获取父类Bean工厂
            BeanFactory parentBeanFactory = getParentBeanFactory();
            //如果父类Bean工厂不为空，并且不存在对应Bean定义
            if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
                //初始化Bean的名称
                String nameToLookup = originalBeanName(name);
                //如果传入的构造参数不为空
                if (args != null) {
                    //根据名称和构造参数获取Bean实例
                    return (T) parentBeanFactory.getBean(nameToLookup, args);
                //如果传入的构造参数为空
                } else {
                    //根据名称和需求类型获取Bean实例
                    return parentBeanFactory.getBean(nameToLookup, requiredType);
                }
            }
            //如果只是进行类型检查
            if (!typeCheckOnly) {
                //标记该Bean已被创建
                markBeanAsCreated(beanName);
            }

            try {
                //获取合并后的本地Bean定义
                final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                //检查合并后的Bean定义
                checkMergedBeanDefinition(mbd, beanName, args);

                //获取所依赖的Bean集合
                String[] dependsOn = mbd.getDependsOn();
                //如果所依赖集合不为空
                if (dependsOn != null) {
                    //遍历所依赖的Bean集合
                    for (String dep : dependsOn) {
                        if (isDependent(beanName, dep)) {
                            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                    "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
                        }
                        //注册Bean的依赖关系
                        registerDependentBean(dep, beanName);
                        //递归获取所依赖Bean的实例
                        getBean(dep);
                    }
                }

                //如果是单例Bean
                if (mbd.isSingleton()) {
                    //从缓存中获取Bean对象，获取不到则通过对象工厂生成
                    sharedInstance = getSingleton(beanName, new ObjectFactory<Object>() {
                        @Override
                        public Object getObject() throws BeansException {
                            try {
                                //创建Bean对象
                                return createBean(beanName, mbd, args);
                            } catch (BeansException ex) {
                                //销毁Bean对象
                                destroySingleton(beanName);
                                throw ex;
                            }
                        }
                    });
                    //获取Bean对象，可通过工厂Bean来创建对象
                    bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
                //如果是原型Bean
                } else if (mbd.isPrototype()) {
                    Object prototypeInstance = null;
                    try {
                        //创建原型Bean之前执行
                        beforePrototypeCreation(beanName);
                        //创建Bean实例
                        prototypeInstance = createBean(beanName, mbd, args);
                    } finally {
                        //创建原型Bean之后执行
                        afterPrototypeCreation(beanName);
                    }
                    //获取Bean对象，可通过工厂Bean来创建对象
                    bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
                //如果是其他Bean
                } else {
                    //获取范围名称
                    String scopeName = mbd.getScope();
                    //从缓存中获取对应的范围
                    final Scope scope = this.scopes.get(scopeName);
                    if (scope == null) {
                        throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
                    }
                    try {
                        Object scopedInstance = scope.get(beanName, new ObjectFactory<Object>() {
                            @Override
                            public Object getObject() throws BeansException {
                                beforePrototypeCreation(beanName);
                                try {
                                    return createBean(beanName, mbd, args);
                                } finally {
                                    afterPrototypeCreation(beanName);
                                }
                            }
                        });
                        //获取Bean对象，可通过工厂Bean来创建对象
                        bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
                    } catch (IllegalStateException ex) {
                        throw new BeanCreationException(beanName, "Scope '" + scopeName
                                + "' is not active for the current thread; consider "
                                + "defining a scoped proxy for this bean if you intend to refer to it from a singleton",
                                ex);
                    }
                }
            } catch (BeansException ex) {
                //Bean创建失败后执行清理操作
                cleanupAfterBeanCreationFailure(beanName);
                throw ex;
            }
        }

        //检查实际Bean类型是否和要求类型匹配
        if (requiredType != null && bean != null && !requiredType.isInstance(bean)) {
            try {
                //若类型不匹配则获取类型转换器进行转换
                return getTypeConverter().convertIfNecessary(bean, requiredType);
            } catch (TypeMismatchException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to convert bean '" + name + "' to required type '"
                            + ClassUtils.getQualifiedName(requiredType) + "'", ex);
                }
                throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
            }
        }
        //如果类型匹配则返回Bean实例
        return (T) bean;
    }

    //是否包含指定Bean
    @Override
    public boolean containsBean(String name) {
        String beanName = transformedBeanName(name);
        if (containsSingleton(beanName) || containsBeanDefinition(beanName)) {
            return (!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(name));
        }
        // Not found -> check parent.
        BeanFactory parentBeanFactory = getParentBeanFactory();
        return (parentBeanFactory != null && parentBeanFactory.containsBean(originalBeanName(name)));
    }

    //是否是单例
    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);

        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            if (beanInstance instanceof FactoryBean) {
                return (BeanFactoryUtils.isFactoryDereference(name) || ((FactoryBean<?>) beanInstance).isSingleton());
            } else {
                return !BeanFactoryUtils.isFactoryDereference(name);
            }
        } else if (containsSingleton(beanName)) {
            return true;
        }

        // No singleton instance found -> check bean definition.
        BeanFactory parentBeanFactory = getParentBeanFactory();
        if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
            // No bean definition found in this factory -> delegate to parent.
            return parentBeanFactory.isSingleton(originalBeanName(name));
        }

        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

        // In case of FactoryBean, return singleton status of created object if not a
        // dereference.
        if (mbd.isSingleton()) {
            if (isFactoryBean(beanName, mbd)) {
                if (BeanFactoryUtils.isFactoryDereference(name)) {
                    return true;
                }
                FactoryBean<?> factoryBean = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
                return factoryBean.isSingleton();
            } else {
                return !BeanFactoryUtils.isFactoryDereference(name);
            }
        } else {
            return false;
        }
    }

    //是否是原型
    @Override
    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        //提取不带"&"前缀的Bean名称
        String beanName = transformedBeanName(name);
        //获取父类Bean工厂
        BeanFactory parentBeanFactory = getParentBeanFactory();
        if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
            // No bean definition found in this factory -> delegate to parent.
            return parentBeanFactory.isPrototype(originalBeanName(name));
        }

        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        if (mbd.isPrototype()) {
            // In case of FactoryBean, return singleton status of created object if not a
            // dereference.
            return (!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(beanName, mbd));
        }

        // Singleton or scoped - not a prototype.
        // However, FactoryBean may still produce a prototype object...
        if (BeanFactoryUtils.isFactoryDereference(name)) {
            return false;
        }
        if (isFactoryBean(beanName, mbd)) {
            final FactoryBean<?> fb = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
            if (System.getSecurityManager() != null) {
                return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                    @Override
                    public Boolean run() {
                        return ((fb instanceof SmartFactoryBean && ((SmartFactoryBean<?>) fb).isPrototype())
                                || !fb.isSingleton());
                    }
                }, getAccessControlContext());
            } else {
                return ((fb instanceof SmartFactoryBean && ((SmartFactoryBean<?>) fb).isPrototype())
                        || !fb.isSingleton());
            }
        } else {
            return false;
        }
    }

    //是否类型匹配
    @Override
    public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
        //提取不带"&"前缀的Bean名称
        String beanName = transformedBeanName(name);
        //根据Bean名称获取对应Bean对象
        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            //若所获对象为工厂Bean对象
            if (beanInstance instanceof FactoryBean) {
                //若name不是工厂Bean名称
                if (!BeanFactoryUtils.isFactoryDereference(name)) {
                    //从工厂Bean中获取对象类型
                    Class<?> type = getTypeForFactoryBean((FactoryBean<?>) beanInstance);
                    //进行类型匹配并返回结果
                    return (type != null && typeToMatch.isAssignableFrom(type));
                } else {
                    //否则，直接判断是否是匹配类型的实例
                    return typeToMatch.isInstance(beanInstance);
                }
            } else if (!BeanFactoryUtils.isFactoryDereference(name)) {
                if (typeToMatch.isInstance(beanInstance)) {
                    return true;
                } else if (typeToMatch.hasGenerics() && containsBeanDefinition(beanName)) {
                    //获取合并后的Bean定义
                    RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                    Class<?> targetType = mbd.getTargetType();
                    if (targetType != null && targetType != ClassUtils.getUserClass(beanInstance)
                            && typeToMatch.isAssignableFrom(targetType)) {
                        // Check raw class match as well, making sure it's exposed on the proxy.
                        Class<?> classToMatch = typeToMatch.resolve();
                        return (classToMatch == null || classToMatch.isInstance(beanInstance));
                    }
                }
            }
            return false;
        } else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
            return false;
        }

        //获取父类Bean工厂
        BeanFactory parentBeanFactory = getParentBeanFactory();
        if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
            // No bean definition found in this factory -> delegate to parent.
            return parentBeanFactory.isTypeMatch(originalBeanName(name), typeToMatch);
        }

        //获取合并后的Bean定义
        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

        Class<?> classToMatch = typeToMatch.resolve();
        if (classToMatch == null) {
            classToMatch = FactoryBean.class;
        }
        Class<?>[] typesToMatch = (FactoryBean.class == classToMatch ? new Class<?>[]{classToMatch}
                : new Class<?>[]{FactoryBean.class, classToMatch});

        // Check decorated bean definition, if any: We assume it'll be easier
        // to determine the decorated bean's type than the proxy's type.
        BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
        if (dbd != null && !BeanFactoryUtils.isFactoryDereference(name)) {
            RootBeanDefinition tbd = getMergedBeanDefinition(dbd.getBeanName(), dbd.getBeanDefinition(), mbd);
            Class<?> targetClass = predictBeanType(dbd.getBeanName(), tbd, typesToMatch);
            if (targetClass != null && !FactoryBean.class.isAssignableFrom(targetClass)) {
                return typeToMatch.isAssignableFrom(targetClass);
            }
        }
        //预测Bean的类型
        Class<?> beanType = predictBeanType(beanName, mbd, typesToMatch);
        if (beanType == null) {
            return false;
        }

        // Check bean class whether we're dealing with a FactoryBean.
        if (FactoryBean.class.isAssignableFrom(beanType)) {
            if (!BeanFactoryUtils.isFactoryDereference(name)) {
                // If it's a FactoryBean, we want to look at what it creates, not the factory
                // class.
                beanType = getTypeForFactoryBean(beanName, mbd);
                if (beanType == null) {
                    return false;
                }
            }
        } else if (BeanFactoryUtils.isFactoryDereference(name)) {
            // Special case: A SmartInstantiationAwareBeanPostProcessor returned a
            // non-FactoryBean
            // type but we nevertheless are being asked to dereference a FactoryBean...
            // Let's check the original bean class and proceed with it if it is a
            // FactoryBean.
            beanType = predictBeanType(beanName, mbd, FactoryBean.class);
            if (beanType == null || !FactoryBean.class.isAssignableFrom(beanType)) {
                return false;
            }
        }

        ResolvableType resolvableType = mbd.targetType;
        if (resolvableType == null) {
            resolvableType = mbd.factoryMethodReturnType;
        }
        if (resolvableType != null && resolvableType.resolve() == beanType) {
            return typeToMatch.isAssignableFrom(resolvableType);
        }
        return typeToMatch.isAssignableFrom(beanType);
    }

    @Override
    public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        return isTypeMatch(name, ResolvableType.forRawClass(typeToMatch));
    }

    //获取Bean的类型
    @Override
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        //转换处理成Bean名称
        String beanName = transformedBeanName(name);
        //获取单例Bean对象
        Object beanInstance = getSingleton(beanName, false);
        if (beanInstance != null) {
            if (beanInstance instanceof FactoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
                return getTypeForFactoryBean((FactoryBean<?>) beanInstance);
            } else {
                return beanInstance.getClass();
            }
        } else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
            return null;
        }

        //获取父类Bean工厂
        BeanFactory parentBeanFactory = getParentBeanFactory();
        //父类Bean工厂不为空，并且不包含Bean定义
        if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
            //通过父类Bean工厂获取类型
            return parentBeanFactory.getType(originalBeanName(name));
        }

        //获取合并后的Bean定义
        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        //获取Bean定义持有器
        BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
        if (dbd != null && !BeanFactoryUtils.isFactoryDereference(name)) {
            //获取合并后的Bean定义
            RootBeanDefinition tbd = getMergedBeanDefinition(dbd.getBeanName(), dbd.getBeanDefinition(), mbd);
            //预测Bean的类型
            Class<?> targetClass = predictBeanType(dbd.getBeanName(), tbd);
            if (targetClass != null && !FactoryBean.class.isAssignableFrom(targetClass)) {
                return targetClass;
            }
        }
        //预测Bean的类型
        Class<?> beanClass = predictBeanType(beanName, mbd);

        // Check bean class whether we're dealing with a FactoryBean.
        if (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass)) {
            if (!BeanFactoryUtils.isFactoryDereference(name)) {
                return getTypeForFactoryBean(beanName, mbd);
            } else {
                return beanClass;
            }
        } else {
            return (!BeanFactoryUtils.isFactoryDereference(name) ? beanClass : null);
        }
    }

    //获取所有别名集合
    @Override
    public String[] getAliases(String name) {
        String beanName = transformedBeanName(name);
        List<String> aliases = new ArrayList<String>();
        boolean factoryPrefix = name.startsWith(FACTORY_BEAN_PREFIX);
        String fullBeanName = beanName;
        if (factoryPrefix) {
            fullBeanName = FACTORY_BEAN_PREFIX + beanName;
        }
        if (!fullBeanName.equals(name)) {
            aliases.add(fullBeanName);
        }
        String[] retrievedAliases = super.getAliases(beanName);
        for (String retrievedAlias : retrievedAliases) {
            String alias = (factoryPrefix ? FACTORY_BEAN_PREFIX : "") + retrievedAlias;
            if (!alias.equals(name)) {
                aliases.add(alias);
            }
        }
        if (!containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
            BeanFactory parentBeanFactory = getParentBeanFactory();
            if (parentBeanFactory != null) {
                aliases.addAll(Arrays.asList(parentBeanFactory.getAliases(fullBeanName)));
            }
        }
        return StringUtils.toStringArray(aliases);
    }

    // ---------------------------------------------------------------------
    // 实现HierarchicalBeanFactory接口方法
    // ---------------------------------------------------------------------

    //获取父类Bean工厂
    @Override
    public BeanFactory getParentBeanFactory() {
        return this.parentBeanFactory;
    }

    //是否包含本地Bean
    @Override
    public boolean containsLocalBean(String name) {
        String beanName = transformedBeanName(name);
        return ((containsSingleton(beanName) || containsBeanDefinition(beanName))
                && (!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(beanName)));
    }

    // ---------------------------------------------------------------------
    // 实现ConfigurableBeanFactory接口方法
    // ---------------------------------------------------------------------

    //设置父类Bean工厂
    @Override
    public void setParentBeanFactory(BeanFactory parentBeanFactory) {
        if (this.parentBeanFactory != null && this.parentBeanFactory != parentBeanFactory) {
            throw new IllegalStateException("Already associated with parent BeanFactory: " + this.parentBeanFactory);
        }
        this.parentBeanFactory = parentBeanFactory;
    }

    //设置类加载器
    @Override
    public void setBeanClassLoader(ClassLoader beanClassLoader) {
        this.beanClassLoader = (beanClassLoader != null ? beanClassLoader : ClassUtils.getDefaultClassLoader());
    }

    //获取类加载器
    @Override
    public ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

    //设置临时类加载器
    @Override
    public void setTempClassLoader(ClassLoader tempClassLoader) {
        this.tempClassLoader = tempClassLoader;
    }

    //获取临时类加载器
    @Override
    public ClassLoader getTempClassLoader() {
        return this.tempClassLoader;
    }

    //设置是否缓存Bean的元数据
    @Override
    public void setCacheBeanMetadata(boolean cacheBeanMetadata) {
        this.cacheBeanMetadata = cacheBeanMetadata;
    }

    //是否缓存Bean的元数据
    @Override
    public boolean isCacheBeanMetadata() {
        return this.cacheBeanMetadata;
    }

    //设置Bean表达式解析器
    @Override
    public void setBeanExpressionResolver(BeanExpressionResolver resolver) {
        this.beanExpressionResolver = resolver;
    }

    //获取Bean表达式解析器
    @Override
    public BeanExpressionResolver getBeanExpressionResolver() {
        return this.beanExpressionResolver;
    }

    //设置转换服务器
    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    //获取转换服务器
    @Override
    public ConversionService getConversionService() {
        return this.conversionService;
    }

    //添加属性编辑器注册器
    @Override
    public void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar) {
        Assert.notNull(registrar, "PropertyEditorRegistrar must not be null");
        this.propertyEditorRegistrars.add(registrar);
    }

    //获取属性编辑器注册器
    public Set<PropertyEditorRegistrar> getPropertyEditorRegistrars() {
        return this.propertyEditorRegistrars;
    }

    //注册外部编辑器
    @Override
    public void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass) {
        Assert.notNull(requiredType, "Required type must not be null");
        Assert.notNull(propertyEditorClass, "PropertyEditor class must not be null");
        this.customEditors.put(requiredType, propertyEditorClass);
    }

    //复制注册的编辑器
    @Override
    public void copyRegisteredEditorsTo(PropertyEditorRegistry registry) {
        registerCustomEditors(registry);
    }

    //获取外部编辑器
    public Map<Class<?>, Class<? extends PropertyEditor>> getCustomEditors() {
        return this.customEditors;
    }

    //设置外部类型转换器
    @Override
    public void setTypeConverter(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }

    //获取外部类型转换器
    protected TypeConverter getCustomTypeConverter() {
        return this.typeConverter;
    }

    //获取类型转换器
    @Override
    public TypeConverter getTypeConverter() {
        TypeConverter customConverter = getCustomTypeConverter();
        if (customConverter != null) {
            return customConverter;
        } else {
            // Build default TypeConverter, registering custom editors.
            SimpleTypeConverter typeConverter = new SimpleTypeConverter();
            typeConverter.setConversionService(getConversionService());
            registerCustomEditors(typeConverter);
            return typeConverter;
        }
    }

    //添加嵌入值解析器
    @Override
    public void addEmbeddedValueResolver(StringValueResolver valueResolver) {
        Assert.notNull(valueResolver, "StringValueResolver must not be null");
        this.embeddedValueResolvers.add(valueResolver);
    }

    //是否有嵌入值解析器
    @Override
    public boolean hasEmbeddedValueResolver() {
        return !this.embeddedValueResolvers.isEmpty();
    }

    //解析嵌入的值
    @Override
    public String resolveEmbeddedValue(String value) {
        if (value == null) {
            return null;
        }
        String result = value;
        for (StringValueResolver resolver : this.embeddedValueResolvers) {
            result = resolver.resolveStringValue(result);
            if (result == null) {
                return null;
            }
        }
        return result;
    }

    //添加Bean后置加工器
    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        Assert.notNull(beanPostProcessor, "BeanPostProcessor must not be null");
        this.beanPostProcessors.remove(beanPostProcessor);
        this.beanPostProcessors.add(beanPostProcessor);
        if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
            this.hasInstantiationAwareBeanPostProcessors = true;
        }
        if (beanPostProcessor instanceof DestructionAwareBeanPostProcessor) {
            this.hasDestructionAwareBeanPostProcessors = true;
        }
    }

    //获取Bean后置加工器数量
    @Override
    public int getBeanPostProcessorCount() {
        return this.beanPostProcessors.size();
    }

    //获取Bean后置加工器集合
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    //是否实例化后置处理器
    protected boolean hasInstantiationAwareBeanPostProcessors() {
        return this.hasInstantiationAwareBeanPostProcessors;
    }

    //是否已销毁后置处理器
    protected boolean hasDestructionAwareBeanPostProcessors() {
        return this.hasDestructionAwareBeanPostProcessors;
    }

    //注册范围
    @Override
    public void registerScope(String scopeName, Scope scope) {
        Assert.notNull(scopeName, "Scope identifier must not be null");
        Assert.notNull(scope, "Scope must not be null");
        if (SCOPE_SINGLETON.equals(scopeName) || SCOPE_PROTOTYPE.equals(scopeName)) {
            throw new IllegalArgumentException("Cannot replace existing scopes 'singleton' and 'prototype'");
        }
        Scope previous = this.scopes.put(scopeName, scope);
        if (previous != null && previous != scope) {
            if (logger.isInfoEnabled()) {
                logger.info("Replacing scope '" + scopeName + "' from [" + previous + "] to [" + scope + "]");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Registering scope '" + scopeName + "' with implementation [" + scope + "]");
            }
        }
    }

    //获取已注册的范围名称
    @Override
    public String[] getRegisteredScopeNames() {
        return StringUtils.toStringArray(this.scopes.keySet());
    }

    //获取指定名称的范围
    @Override
    public Scope getRegisteredScope(String scopeName) {
        Assert.notNull(scopeName, "Scope identifier must not be null");
        return this.scopes.get(scopeName);
    }

    //设置安全上下文提供者
    public void setSecurityContextProvider(SecurityContextProvider securityProvider) {
        this.securityContextProvider = securityProvider;
    }

    //获取访问控制上下文
    @Override
    public AccessControlContext getAccessControlContext() {
        return (this.securityContextProvider != null ? this.securityContextProvider.getAccessControlContext()
                : AccessController.getContext());
    }

    //复制配置信息
    @Override
    public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
        Assert.notNull(otherFactory, "BeanFactory must not be null");
        setBeanClassLoader(otherFactory.getBeanClassLoader());
        setCacheBeanMetadata(otherFactory.isCacheBeanMetadata());
        setBeanExpressionResolver(otherFactory.getBeanExpressionResolver());
        setConversionService(otherFactory.getConversionService());
        if (otherFactory instanceof AbstractBeanFactory) {
            AbstractBeanFactory otherAbstractFactory = (AbstractBeanFactory) otherFactory;
            this.propertyEditorRegistrars.addAll(otherAbstractFactory.propertyEditorRegistrars);
            this.customEditors.putAll(otherAbstractFactory.customEditors);
            this.typeConverter = otherAbstractFactory.typeConverter;
            this.beanPostProcessors.addAll(otherAbstractFactory.beanPostProcessors);
            this.hasInstantiationAwareBeanPostProcessors = this.hasInstantiationAwareBeanPostProcessors
                    || otherAbstractFactory.hasInstantiationAwareBeanPostProcessors;
            this.hasDestructionAwareBeanPostProcessors = this.hasDestructionAwareBeanPostProcessors
                    || otherAbstractFactory.hasDestructionAwareBeanPostProcessors;
            this.scopes.putAll(otherAbstractFactory.scopes);
            this.securityContextProvider = otherAbstractFactory.securityContextProvider;
        } else {
            setTypeConverter(otherFactory.getTypeConverter());
            String[] otherScopeNames = otherFactory.getRegisteredScopeNames();
            for (String scopeName : otherScopeNames) {
                this.scopes.put(scopeName, otherFactory.getRegisteredScope(scopeName));
            }
        }
    }

    //获取合并后的Bean定义
    @Override
    public BeanDefinition getMergedBeanDefinition(String name) throws BeansException {
        //提取不带"&"前缀的Bean名称
        String beanName = transformedBeanName(name);
        //若不存在Bean定义，且父类Bean工厂是ConfigurableBeanFactory
        if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableBeanFactory) {
            //通过父类Bean工厂来合并Bean定义
            return ((ConfigurableBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(beanName);
        }
        //直接获取根级Bean定义
        return getMergedLocalBeanDefinition(beanName);
    }

    //是否是工厂Bean
    @Override
    public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
        //提取不带"&"前缀的Bean名称
        String beanName = transformedBeanName(name);
        //通过Bean名称获取Bean实例
        Object beanInstance = getSingleton(beanName, false);
        //若获取的Bean实例不为空，则直接进行类型判断
        if (beanInstance != null) {
            return (beanInstance instanceof FactoryBean);
        //若包含该Bean名称，则注册的是空对象
        } else if (containsSingleton(beanName)) {
            //因此这里返回false
            return false;
        }

        //若不存在相应的Bean定义，且父类Bean工厂实现了ConfigurableBeanFactory接口
        if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableBeanFactory) {
            //则通过父类Bean工厂来判断是否是工厂Bean
            return ((ConfigurableBeanFactory) getParentBeanFactory()).isFactoryBean(name);
        }
        //否则，获取根级Bean定义后再次进行判断
        return isFactoryBean(beanName, getMergedLocalBeanDefinition(beanName));
    }

    //该Bean是否正在创建
    @Override
    public boolean isActuallyInCreation(String beanName) {
        return (isSingletonCurrentlyInCreation(beanName) || isPrototypeCurrentlyInCreation(beanName));
    }

    //当前创建的是否是原型Bean
    protected boolean isPrototypeCurrentlyInCreation(String beanName) {
        Object curVal = this.prototypesCurrentlyInCreation.get();
        return (curVal != null
                && (curVal.equals(beanName) || (curVal instanceof Set && ((Set<?>) curVal).contains(beanName))));
    }

    //原型Bean创建之前执行
    @SuppressWarnings("unchecked")
    protected void beforePrototypeCreation(String beanName) {
        Object curVal = this.prototypesCurrentlyInCreation.get();
        if (curVal == null) {
            this.prototypesCurrentlyInCreation.set(beanName);
        } else if (curVal instanceof String) {
            Set<String> beanNameSet = new HashSet<String>(2);
            beanNameSet.add((String) curVal);
            beanNameSet.add(beanName);
            this.prototypesCurrentlyInCreation.set(beanNameSet);
        } else {
            Set<String> beanNameSet = (Set<String>) curVal;
            beanNameSet.add(beanName);
        }
    }

    //原型Bean创建之后执行
    @SuppressWarnings("unchecked")
    protected void afterPrototypeCreation(String beanName) {
        Object curVal = this.prototypesCurrentlyInCreation.get();
        if (curVal instanceof String) {
            this.prototypesCurrentlyInCreation.remove();
        } else if (curVal instanceof Set) {
            Set<String> beanNameSet = (Set<String>) curVal;
            beanNameSet.remove(beanName);
            if (beanNameSet.isEmpty()) {
                this.prototypesCurrentlyInCreation.remove();
            }
        }
    }

    //销毁Bean实例
    @Override
    public void destroyBean(String beanName, Object beanInstance) {
        destroyBean(beanName, beanInstance, getMergedLocalBeanDefinition(beanName));
    }

    //销毁Bean实例
    protected void destroyBean(String beanName, Object bean, RootBeanDefinition mbd) {
        new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), getAccessControlContext()).destroy();
    }

    //销毁范围Bean
    @Override
    public void destroyScopedBean(String beanName) {
        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        if (mbd.isSingleton() || mbd.isPrototype()) {
            throw new IllegalArgumentException(
                    "Bean name '" + beanName + "' does not correspond to an object in a mutable scope");
        }
        String scopeName = mbd.getScope();
        Scope scope = this.scopes.get(scopeName);
        if (scope == null) {
            throw new IllegalStateException("No Scope SPI registered for scope name '" + scopeName + "'");
        }
        Object bean = scope.remove(beanName);
        if (bean != null) {
            destroyBean(beanName, bean, mbd);
        }
    }

    // ---------------------------------------------------------------------
    // Implementation methods
    // ---------------------------------------------------------------------

    //提取不带"&"前缀的Bean名称
    protected String transformedBeanName(String name) {
        //截取工厂Bean前缀，并根据截取后的Bean名称获取别名
        return canonicalName(BeanFactoryUtils.transformedBeanName(name));
    }

    //原始Bean的名称
    protected String originalBeanName(String name) {
        //提取不带"&"前缀的Bean名称
        String beanName = transformedBeanName(name);
        //如果name是工厂Bean名称，则将工厂Bean前缀拼接回去
        if (name.startsWith(FACTORY_BEAN_PREFIX)) {
            beanName = FACTORY_BEAN_PREFIX + beanName;
        }
        //返回原始名称
        return beanName;
    }

    //初始化Bean包装器
    protected void initBeanWrapper(BeanWrapper bw) {
        //设置转换服务者
        bw.setConversionService(getConversionService());
        //注册外部属性编辑器
        registerCustomEditors(bw);
    }

    //注册编辑器
    protected void registerCustomEditors(PropertyEditorRegistry registry) {
        PropertyEditorRegistrySupport registrySupport = (registry instanceof PropertyEditorRegistrySupport
                ? (PropertyEditorRegistrySupport) registry
                : null);
        if (registrySupport != null) {
            registrySupport.useConfigValueEditors();
        }
        if (!this.propertyEditorRegistrars.isEmpty()) {
            for (PropertyEditorRegistrar registrar : this.propertyEditorRegistrars) {
                try {
                    registrar.registerCustomEditors(registry);
                } catch (BeanCreationException ex) {
                    Throwable rootCause = ex.getMostSpecificCause();
                    if (rootCause instanceof BeanCurrentlyInCreationException) {
                        BeanCreationException bce = (BeanCreationException) rootCause;
                        if (isCurrentlyInCreation(bce.getBeanName())) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("PropertyEditorRegistrar [" + registrar.getClass().getName()
                                        + "] failed because it tried to obtain currently created bean '"
                                        + ex.getBeanName() + "': " + ex.getMessage());
                            }
                            onSuppressedException(ex);
                            continue;
                        }
                    }
                    throw ex;
                }
            }
        }
        if (!this.customEditors.isEmpty()) {
            for (Map.Entry<Class<?>, Class<? extends PropertyEditor>> entry : this.customEditors.entrySet()) {
                Class<?> requiredType = entry.getKey();
                Class<? extends PropertyEditor> editorClass = entry.getValue();
                registry.registerCustomEditor(requiredType, BeanUtils.instantiateClass(editorClass));
            }
        }
    }


    //获取根级Bean定义
    protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
        //根据Bean名称从缓存中获取根级Bean定义
        RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
        //若不为空则返回该Bean定义
        if (mbd != null) {
            return mbd;
        }
        //否则执行方法获取合并后的Bean定义
        return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
    }

    //获取根级Bean定义
    protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd)
            throws BeanDefinitionStoreException {
        return getMergedBeanDefinition(beanName, bd, null);
    }

    //获取根级Bean定义
    protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd,
                                                         BeanDefinition containingBd) throws BeanDefinitionStoreException {

        synchronized (this.mergedBeanDefinitions) {
            RootBeanDefinition mbd = null;
            //如果包含的Bean定义为空
            if (containingBd == null) {
                //根据Bean名称从缓存中获取Bean定义
                mbd = this.mergedBeanDefinitions.get(beanName);
            }

            if (mbd == null) {
                //如果该Bean定义没有父级Bean定义
                if (bd.getParentName() == null) {
                    //若是根级Bean定义，则转换成根级Bean定义然后进行克隆
                    if (bd instanceof RootBeanDefinition) {
                        mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
                    //若不是根级Bean定义，则深度复制该Bean定义的配置
                    } else {
                        mbd = new RootBeanDefinition(bd);
                    }
                //如果该Bean定义存在父级Bean定义
                } else {
                    BeanDefinition pbd;
                    try {
                        //获取父类Bean定义名称
                        String parentBeanName = transformedBeanName(bd.getParentName());
                        //若Bean名称不等于父类Bean名称
                        if (!beanName.equals(parentBeanName)) {
                            pbd = getMergedBeanDefinition(parentBeanName);
                        } else {
                            //获取父类Bean工厂
                            BeanFactory parent = getParentBeanFactory();
                            //如果父类Bean工厂实现了ConfigurableBeanFactory接口
                            if (parent instanceof ConfigurableBeanFactory) {
                                pbd = ((ConfigurableBeanFactory) parent).getMergedBeanDefinition(parentBeanName);
                            } else {
                                throw new NoSuchBeanDefinitionException(parentBeanName,
                                        "Parent name '" + parentBeanName + "' is equal to bean name '" + beanName
                                                + "': cannot be resolved without an AbstractBeanFactory parent");
                            }
                        }
                    } catch (NoSuchBeanDefinitionException ex) {
                        throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
                                "Could not resolve parent bean definition '" + bd.getParentName() + "'", ex);
                    }
                    //深度复制父类Bean定义的配置
                    mbd = new RootBeanDefinition(pbd);
                    //使用子类Bean定义覆盖当前配置
                    mbd.overrideFrom(bd);
                }

                //若Bean定义未设置范围，则默认为单例范围
                if (!StringUtils.hasLength(mbd.getScope())) {
                    mbd.setScope(RootBeanDefinition.SCOPE_SINGLETON);
                }

                //若所包含的Bean不是单例，而当前Bean是单例，则采用所包含Bean的范围
                if (containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()) {
                    mbd.setScope(containingBd.getScope());
                }

                //如果需要缓存Bean的元数据，则将其放入映射中
                if (containingBd == null && isCacheBeanMetadata()) {
                    this.mergedBeanDefinitions.put(beanName, mbd);
                }
            }

            return mbd;
        }
    }

    //检查合并后的Bean定义
    protected void checkMergedBeanDefinition(RootBeanDefinition mbd, String beanName, Object[] args)
            throws BeanDefinitionStoreException {
        if (mbd.isAbstract()) {
            throw new BeanIsAbstractException(beanName);
        }
    }

    //清空合并后的Bean定义
    protected void clearMergedBeanDefinition(String beanName) {
        this.mergedBeanDefinitions.remove(beanName);
    }

    //清空元数据缓存
    public void clearMetadataCache() {
        Iterator<String> mergedBeans = this.mergedBeanDefinitions.keySet().iterator();
        while (mergedBeans.hasNext()) {
            if (!isBeanEligibleForMetadataCaching(mergedBeans.next())) {
                mergedBeans.remove();
            }
        }
    }

    //确定Bean的类型
    protected Class<?> resolveBeanClass(final RootBeanDefinition mbd, String beanName, final Class<?>... typesToMatch)
            throws CannotLoadBeanClassException {
        try {
            if (mbd.hasBeanClass()) {
                return mbd.getBeanClass();
            }
            //若安全管理器不为空，则在安全控制下解析Bean类型
            if (System.getSecurityManager() != null) {
                return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
                    @Override
                    public Class<?> run() throws Exception {
                        return doResolveBeanClass(mbd, typesToMatch);
                    }
                }, getAccessControlContext());
            //否则，在非安全控制下解析Bean类型
            } else {
                return doResolveBeanClass(mbd, typesToMatch);
            }
        } catch (PrivilegedActionException pae) {
            ClassNotFoundException ex = (ClassNotFoundException) pae.getException();
            throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
        } catch (ClassNotFoundException ex) {
            throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
        } catch (LinkageError ex) {
            throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
        }
    }

    //核心确定Bean类型方法
    private Class<?> doResolveBeanClass(RootBeanDefinition mbd, Class<?>... typesToMatch)
            throws ClassNotFoundException {
        //获取Bean的类加载器
        ClassLoader beanClassLoader = getBeanClassLoader();
        ClassLoader classLoaderToUse = beanClassLoader;
        //如果要匹配的类型不为空
        if (!ObjectUtils.isEmpty(typesToMatch)) {
            // When just doing type checks (i.e. not creating an actual instance yet),
            // use the specified temporary class loader (e.g. in a weaving scenario).
            //获取临时类加载器
            ClassLoader tempClassLoader = getTempClassLoader();
            if (tempClassLoader != null) {
                classLoaderToUse = tempClassLoader;
                if (tempClassLoader instanceof DecoratingClassLoader) {
                    DecoratingClassLoader dcl = (DecoratingClassLoader) tempClassLoader;
                    //遍历要匹配的类型，排除这些类型
                    for (Class<?> typeToMatch : typesToMatch) {
                        dcl.excludeClass(typeToMatch.getName());
                    }
                }
            }
        }
        String className = mbd.getBeanClassName();
        if (className != null) {
            Object evaluated = evaluateBeanDefinitionString(className, mbd);
            if (!className.equals(evaluated)) {
                // A dynamically resolved expression, supported as of 4.2...
                if (evaluated instanceof Class) {
                    return (Class<?>) evaluated;
                } else if (evaluated instanceof String) {
                    return ClassUtils.forName((String) evaluated, classLoaderToUse);
                } else {
                    throw new IllegalStateException("Invalid class name expression result: " + evaluated);
                }
            }
            // When resolving against a temporary class loader, exit early in order
            // to avoid storing the resolved Class in the bean definition.
            if (classLoaderToUse != beanClassLoader) {
                return ClassUtils.forName(className, classLoaderToUse);
            }
        }
        return mbd.resolveBeanClass(beanClassLoader);
    }

    //计算Bean定义的字符串
    protected Object evaluateBeanDefinitionString(String value, BeanDefinition beanDefinition) {
        if (this.beanExpressionResolver == null) {
            return value;
        }
        Scope scope = (beanDefinition != null ? getRegisteredScope(beanDefinition.getScope()) : null);
        return this.beanExpressionResolver.evaluate(value, new BeanExpressionContext(this, scope));
    }

    //预测Bean类型
    protected Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
        //通过根级Bean定义获取目标类型
        Class<?> targetType = mbd.getTargetType();
        //若目标类型不为空，则直接返回目标类型
        if (targetType != null) {
            return targetType;
        }
        //若工厂方法名不为空，则直接返回null
        if (mbd.getFactoryMethodName() != null) {
            return null;
        }
        //否则，解析Bean的类型
        return resolveBeanClass(mbd, beanName, typesToMatch);
    }

    //是否是工厂Bean
    protected boolean isFactoryBean(String beanName, RootBeanDefinition mbd) {
        //对Bean的类型进行预测
        Class<?> beanType = predictBeanType(beanName, mbd, FactoryBean.class);
        //若预测类型不为空，则判断预测类型是否是FactoryBean
        return (beanType != null && FactoryBean.class.isAssignableFrom(beanType));
    }

    //获取工厂Bean的类型
    protected Class<?> getTypeForFactoryBean(String beanName, RootBeanDefinition mbd) {
        if (!mbd.isSingleton()) {
            return null;
        }
        try {
            //首先获取工厂Bean对象
            FactoryBean<?> factoryBean = doGetBean(FACTORY_BEAN_PREFIX + beanName, FactoryBean.class, null, true);
            //其次获取工厂Bean类型
            return getTypeForFactoryBean(factoryBean);
        } catch (BeanCreationException ex) {
            if (ex instanceof BeanCurrentlyInCreationException) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Bean currently in creation on FactoryBean type check: " + ex);
                }
            } else if (mbd.isLazyInit()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Bean creation exception on lazy FactoryBean type check: " + ex);
                }
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Bean creation exception on non-lazy FactoryBean type check: " + ex);
                }
            }
            onSuppressedException(ex);
            return null;
        }
    }

    //标记Bean已经创建
    protected void markBeanAsCreated(String beanName) {
        //如果该Bean不存在已创建Bean集合中
        if (!this.alreadyCreated.contains(beanName)) {
            synchronized (this.mergedBeanDefinitions) {
                //再次判断是否存在已创建Bean集合中
                if (!this.alreadyCreated.contains(beanName)) {
                    //清除合并的Bean定义
                    clearMergedBeanDefinition(beanName);
                    //添加到已创建Bean集合中
                    this.alreadyCreated.add(beanName);
                }
            }
        }
    }

    //创建Bean失败后进行清理操作
    protected void cleanupAfterBeanCreationFailure(String beanName) {
        synchronized (this.mergedBeanDefinitions) {
            this.alreadyCreated.remove(beanName);
        }
    }

    /**
     * Determine whether the specified bean is eligible for having its bean
     * definition metadata cached.
     *
     * @param beanName the name of the bean
     * @return {@code true} if the bean's metadata may be cached at this point
     * already
     */
    protected boolean isBeanEligibleForMetadataCaching(String beanName) {
        return this.alreadyCreated.contains(beanName);
    }

    /**
     * Remove the singleton instance (if any) for the given bean name, but only if
     * it hasn't been used for other purposes than type checking.
     *
     * @param beanName the name of the bean
     * @return {@code true} if actually removed, {@code false} otherwise
     */
    protected boolean removeSingletonIfCreatedForTypeCheckOnly(String beanName) {
        if (!this.alreadyCreated.contains(beanName)) {
            removeSingleton(beanName);
            return true;
        } else {
            return false;
        }
    }

    //是否已经有创建好的Bean
    protected boolean hasBeanCreationStarted() {
        return !this.alreadyCreated.isEmpty();
    }

    //通过工厂Bean来获取Bean
    protected Object getObjectForBeanInstance(Object beanInstance, String name, String beanName,
                                              RootBeanDefinition mbd) {

        //若想要获取FactoryBean本身，那么beanInstance必须是FactoryBean的实例
        if (BeanFactoryUtils.isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
            throw new BeanIsNotAFactoryException(transformedBeanName(name), beanInstance.getClass());
        }

        //如果beanInstance不是FactoryBean实例，或者要获取的就是FactoryBean实例，那么直接返回就好
        if (!(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)) {
            return beanInstance;
        }

        Object object = null;
        //若Bean定义为空，则根据Bean名称从缓存中获取对象
        if (mbd == null) {
            object = getCachedObjectForFactoryBean(beanName);
        }
        //若缓存中该Bean对象不存在
        if (object == null) {
            //将beanInstance强制转为FactoryBean
            FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
            //判断该Bean是否存在Bean定义
            if (mbd == null && containsBeanDefinition(beanName)) {
                mbd = getMergedLocalBeanDefinition(beanName);
            }
            //判断Bean定义是否是合成的，若是合成的，则不执行后置处理
            boolean synthetic = (mbd != null && mbd.isSynthetic());
            //通过工厂Bean来获取Bean对象
            object = getObjectFromFactoryBean(factory, beanName, !synthetic);
        }
        return object;
    }


    //判断Bean名称是否已使用
    public boolean isBeanNameInUse(String beanName) {
        return isAlias(beanName) || containsLocalBean(beanName) || hasDependentBean(beanName);
    }

    //是否需要销毁
    protected boolean requiresDestruction(Object bean, RootBeanDefinition mbd) {
        return (bean != null
                && (DisposableBeanAdapter.hasDestroyMethod(bean, mbd) || (hasDestructionAwareBeanPostProcessors()
                && DisposableBeanAdapter.hasApplicableProcessors(bean, getBeanPostProcessors()))));
    }

    //注册一次性Bean
    protected void registerDisposableBeanIfNecessary(String beanName, Object bean, RootBeanDefinition mbd) {
        //获取访问控制上下文
        AccessControlContext acc = (System.getSecurityManager() != null ? getAccessControlContext() : null);
        //如果Bean不是原型模式，并且需要被销毁
        if (!mbd.isPrototype() && requiresDestruction(bean, mbd)) {
            //如果该Bean是单例模式
            if (mbd.isSingleton()) {
                registerDisposableBean(beanName,
                        new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), acc));
            //如果该Bean是其他范围
            } else {
                //获取Bean的范围
                Scope scope = this.scopes.get(mbd.getScope());
                if (scope == null) {
                    throw new IllegalStateException("No Scope registered for scope name '" + mbd.getScope() + "'");
                }
                scope.registerDestructionCallback(beanName,
                        new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), acc));
            }
        }
    }

    // ---------------------------------------------------------------------
    // 需被子类实现的抽象方法
    // ---------------------------------------------------------------------

    //是否存在对应的Bean定义
    protected abstract boolean containsBeanDefinition(String beanName);

    //根据Bean名称获取Bean定义
    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    //创建Bean对象
    protected abstract Object createBean(String beanName, RootBeanDefinition mbd, Object[] args) throws BeanCreationException;

}
