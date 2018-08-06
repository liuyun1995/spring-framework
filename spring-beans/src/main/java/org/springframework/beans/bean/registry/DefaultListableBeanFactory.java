package org.springframework.beans.bean.registry;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Provider;

import org.springframework.beans.bean.BeanUtils;
import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.AbstractBeanFactory;
import org.springframework.beans.factory.support.autowire.SimpleAutowireCandidateResolver;
import org.springframework.beans.property.type.TypeConverter;
import org.springframework.beans.exception.BeanCreationException;
import org.springframework.beans.exception.BeanCurrentlyInCreationException;
import org.springframework.beans.exception.BeanDefinitionStoreException;
import org.springframework.beans.exception.BeanNotOfRequiredTypeException;
import org.springframework.beans.exception.CannotLoadBeanClassException;
import org.springframework.beans.bean.factorybean.FactoryBean;
import org.springframework.beans.exception.NoSuchBeanDefinitionException;
import org.springframework.beans.exception.NoUniqueBeanDefinitionException;
import org.springframework.beans.bean.factorybean.SmartFactoryBean;
import org.springframework.beans.bean.definition.BeanDefinition;
import org.springframework.beans.bean.definition.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.beans.factory.support.*;
import org.springframework.beans.bean.definition.AbstractBeanDefinition;
import org.springframework.beans.exception.BeanDefinitionValidationException;
import org.springframework.beans.bean.definition.RootBeanDefinition;
import org.springframework.beans.factory.support.autowire.AutowireCandidateResolver;
import org.springframework.beans.factory.support.autowire.BeanFactoryAware;
import org.springframework.core.OrderComparator;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.UsesJava8;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CompositeIterator;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

//默认可列举Bean工厂
@SuppressWarnings("serial")
public class DefaultListableBeanFactory extends org.springframework.beans.factory.AbstractAutowireCapableBeanFactory
		implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {

	private static Class<?> javaUtilOptionalClass = null;
	private static Class<?> javaxInjectProviderClass = null;

	static {
		try {
			javaUtilOptionalClass = ClassUtils.forName("java.util.Optional", DefaultListableBeanFactory.class.getClassLoader());
		} catch (ClassNotFoundException ex) {
			//Java 8 not available
		}
		try {
			javaxInjectProviderClass = ClassUtils.forName("javax.inject.Provider", DefaultListableBeanFactory.class.getClassLoader());
		} catch (ClassNotFoundException ex) {
			//JSR-330 API not available
		}
	}

	//可序列化工厂集合
	private static final Map<String, Reference<DefaultListableBeanFactory>> serializableFactories = new ConcurrentHashMap<String, Reference<DefaultListableBeanFactory>>(8);

	//序列化id
	private String serializationId;

	//是否允许Bean定义覆盖
	private boolean allowBeanDefinitionOverriding = true;

	//是否允许提前类加载
	private boolean allowEagerClassLoading = true;

	//依赖比较器
	private Comparator<Object> dependencyComparator;

	//自动装配候选者转换器
	private org.springframework.beans.factory.support.autowire.AutowireCandidateResolver autowireCandidateResolver = new org.springframework.beans.factory.support.autowire.SimpleAutowireCandidateResolver();

	//可解析的依赖
	private final Map<Class<?>, Object> resolvableDependencies = new ConcurrentHashMap<Class<?>, Object>(16);

	//Bean定义集合
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>(256);

	//所有Bean名称集合映射
	private final Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<Class<?>, String[]>(64);

	//单例Bean名称集合映射
	private final Map<Class<?>, String[]> singletonBeanNamesByType = new ConcurrentHashMap<Class<?>, String[]>(64);

	//Bean定义名称集合
	private volatile List<String> beanDefinitionNames = new ArrayList<String>(256);

	//手动注册单例名称集合
	private volatile Set<String> manualSingletonNames = new LinkedHashSet<String>(16);

	//已冻结的Bean定义名称
	private volatile String[] frozenBeanDefinitionNames;

	//配置信息是否冻结
	private volatile boolean configurationFrozen = false;


	//构造器1
	public DefaultListableBeanFactory() {
		super();
	}

	//构造器2
	public DefaultListableBeanFactory(BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}

	//设置序列化id
	public void setSerializationId(String serializationId) {
		if (serializationId != null) {
			serializableFactories.put(serializationId, new WeakReference<DefaultListableBeanFactory>(this));
		} else if (this.serializationId != null) {
			serializableFactories.remove(this.serializationId);
		}
		this.serializationId = serializationId;
	}

	//获取序列化id
	public String getSerializationId() {
		return this.serializationId;
	}

	//设置是否允许Bean定义覆盖
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	//是否允许Bean定义覆盖
	public boolean isAllowBeanDefinitionOverriding() {
		return this.allowBeanDefinitionOverriding;
	}

	//设置是否提前进行类加载
	public void setAllowEagerClassLoading(boolean allowEagerClassLoading) {
		this.allowEagerClassLoading = allowEagerClassLoading;
	}

	//是否提前进行类加载
	public boolean isAllowEagerClassLoading() {
		return this.allowEagerClassLoading;
	}

	//设置依赖比较器
	public void setDependencyComparator(Comparator<Object> dependencyComparator) {
		this.dependencyComparator = dependencyComparator;
	}

	//获取依赖比较器
	public Comparator<Object> getDependencyComparator() {
		return this.dependencyComparator;
	}

	//设置自动装配候选转换器
	public void setAutowireCandidateResolver(final org.springframework.beans.factory.support.autowire.AutowireCandidateResolver autowireCandidateResolver) {
		Assert.notNull(autowireCandidateResolver, "AutowireCandidateResolver must not be null");
		//是否实现了BeanFactoryAware接口
		if (autowireCandidateResolver instanceof org.springframework.beans.factory.support.autowire.BeanFactoryAware) {
			if (System.getSecurityManager() != null) {
				AccessController.doPrivileged(new PrivilegedAction<Object>() {
					@Override
					public Object run() {
						//设置默认的Bean工厂
						((org.springframework.beans.factory.support.autowire.BeanFactoryAware) autowireCandidateResolver).setBeanFactory(DefaultListableBeanFactory.this);
						return null;
					}
				}, getAccessControlContext());
			} else {
				((BeanFactoryAware) autowireCandidateResolver).setBeanFactory(this);
			}
		}
		this.autowireCandidateResolver = autowireCandidateResolver;
	}

	//获取自动装配候选转换器
	public org.springframework.beans.factory.support.autowire.AutowireCandidateResolver getAutowireCandidateResolver() {
		return this.autowireCandidateResolver;
	}

	//复制配置信息
	@Override
	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		//对配置信息进行复制
		super.copyConfigurationFrom(otherFactory);
		//是否是DefaultListableBeanFactory实例
		if (otherFactory instanceof DefaultListableBeanFactory) {
			DefaultListableBeanFactory otherListableFactory = (DefaultListableBeanFactory) otherFactory;
			this.allowBeanDefinitionOverriding = otherListableFactory.allowBeanDefinitionOverriding;
			this.allowEagerClassLoading = otherListableFactory.allowEagerClassLoading;
			this.dependencyComparator = otherListableFactory.dependencyComparator;
			//设置自动装配候选解析器
			setAutowireCandidateResolver(BeanUtils.instantiateClass(getAutowireCandidateResolver().getClass()));
			this.resolvableDependencies.putAll(otherListableFactory.resolvableDependencies);
		}
	}

	// ---------------------------------------------------------------------
	// 实现BeanFactory接口方法
	// ---------------------------------------------------------------------

	//获取Bean对象
	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return getBean(requiredType, (Object[]) null);
	}

	//获取Bean对象
	@Override
	public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
		//获取命名Bean持有期
		NamedBeanHolder<T> namedBean = resolveNamedBean(requiredType, args);
		//命名Bean不为空则返回Bean实例
		if (namedBean != null) {
			return namedBean.getBeanInstance();
		}
		//获取父类Bean工厂
		BeanFactory parent = getParentBeanFactory();
		//若父类Bean工厂不为空，则使用Bean工厂获取Bean
		if (parent != null) {
			return parent.getBean(requiredType, args);
		}
		//否则抛出异常
		throw new NoSuchBeanDefinitionException(requiredType);
	}

	// ---------------------------------------------------------------------
	// 实现ListableBeanFactory接口方法
	// ---------------------------------------------------------------------

	//是否包含Bean定义
	@Override
	public boolean containsBeanDefinition(String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		return this.beanDefinitionMap.containsKey(beanName);
	}

	//获取Bean定义总数
	@Override
	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	//获取Bean定义名称集合
	@Override
	public String[] getBeanDefinitionNames() {
		if (this.frozenBeanDefinitionNames != null) {
			return this.frozenBeanDefinitionNames.clone();
		} else {
			return StringUtils.toStringArray(this.beanDefinitionNames);
		}
	}

	//获取Bean定义名称
	@Override
	public String[] getBeanNamesForType(ResolvableType type) {
		return doGetBeanNamesForType(type, true, true);
	}

	//获取Bean定义名称
	@Override
	public String[] getBeanNamesForType(Class<?> type) {
		return getBeanNamesForType(type, true, true);
	}

	//获取Bean定义名称
	@Override
	public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		//配置信息未被冻结，或传入类型为空，或不允许提前初始化
		if (!isConfigurationFrozen() || type == null || !allowEagerInit) {
			return doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, allowEagerInit);
		}
		//获取Bean类型名称映射(根据是否包含非单例而获取不同的缓存)
		Map<Class<?>, String[]> cache = (includeNonSingletons ? this.allBeanNamesByType : this.singletonBeanNamesByType);
		//根据类型获取Bean名称数组
		String[] resolvedBeanNames = cache.get(type);
		if (resolvedBeanNames != null) {
			return resolvedBeanNames;
		}
		resolvedBeanNames = doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, true);
		if (ClassUtils.isCacheSafe(type, getBeanClassLoader())) {
			//将Bean的类型和名称映射放入缓存
			cache.put(type, resolvedBeanNames);
		}
		return resolvedBeanNames;
	}

	//核心获取Bean定义名称
	private String[] doGetBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		List<String> result = new ArrayList<String>();
		//遍历Bean名称集合
		for (String beanName : this.beanDefinitionNames) {
			//如果Bean名称不是别名
			if (!isAlias(beanName)) {
				try {
					RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
					// Only check bean definition if it is complete.
					if (!mbd.isAbstract() && (allowEagerInit
							|| ((mbd.hasBeanClass() || !mbd.isLazyInit() || isAllowEagerClassLoading()))
									&& !requiresEagerInitForType(mbd.getFactoryBeanName()))) {
						// In case of FactoryBean, match object created by FactoryBean.
						boolean isFactoryBean = isFactoryBean(beanName, mbd);
						BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
						boolean matchFound = (allowEagerInit || !isFactoryBean || (dbd != null && !mbd.isLazyInit())
								|| containsSingleton(beanName))
								&& (includeNonSingletons || (dbd != null ? mbd.isSingleton() : isSingleton(beanName)))
								&& isTypeMatch(beanName, type);
						if (!matchFound && isFactoryBean) {
							// In case of FactoryBean, try to match FactoryBean instance itself next.
							beanName = FACTORY_BEAN_PREFIX + beanName;
							matchFound = (includeNonSingletons || mbd.isSingleton()) && isTypeMatch(beanName, type);
						}
						if (matchFound) {
							result.add(beanName);
						}
					}
				} catch (CannotLoadBeanClassException ex) {
					if (allowEagerInit) {
						throw ex;
					}
					// Probably contains a placeholder: let's ignore it for type matching purposes.
					if (this.logger.isDebugEnabled()) {
						this.logger.debug("Ignoring bean class loading failure for bean '" + beanName + "'", ex);
					}
					onSuppressedException(ex);
				} catch (BeanDefinitionStoreException ex) {
					if (allowEagerInit) {
						throw ex;
					}
					// Probably contains a placeholder: let's ignore it for type matching purposes.
					if (this.logger.isDebugEnabled()) {
						this.logger.debug("Ignoring unresolvable metadata in bean definition '" + beanName + "'", ex);
					}
					onSuppressedException(ex);
				}
			}
		}

		//手动注册单例Bean名称
		for (String beanName : this.manualSingletonNames) {
			try {
				// In case of FactoryBean, match object created by FactoryBean.
				if (isFactoryBean(beanName)) {
					if ((includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type)) {
						result.add(beanName);
						// Match found for this bean: do not match FactoryBean itself anymore.
						continue;
					}
					// In case of FactoryBean, try to match FactoryBean itself next.
					beanName = FACTORY_BEAN_PREFIX + beanName;
				}
				// Match raw bean instance (might be raw FactoryBean).
				if (isTypeMatch(beanName, type)) {
					result.add(beanName);
				}
			} catch (NoSuchBeanDefinitionException ex) {
				// Shouldn't happen - probably a result of circular reference resolution...
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to check manually registered singleton with name '" + beanName + "'", ex);
				}
			}
		}

		return StringUtils.toStringArray(result);
	}

	//是否提前对类型初始化
	private boolean requiresEagerInitForType(String factoryBeanName) {
		return (factoryBeanName != null && isFactoryBean(factoryBeanName) && !containsSingleton(factoryBeanName));
	}

	//根据类型获取Bean
	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
		return getBeansOfType(type, true, true);
	}

	//根据类型获取Bean
	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {
		//根据类型获取Bean的名称数组
		String[] beanNames = getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		Map<String, T> result = new LinkedHashMap<String, T>(beanNames.length);
		//遍历Bean的名称数组
		for (String beanName : beanNames) {
			try {
				//建立Bean的名称与实例映射放入缓存
				result.put(beanName, getBean(beanName, type));
			} catch (BeanCreationException ex) {
				Throwable rootCause = ex.getMostSpecificCause();
				if (rootCause instanceof BeanCurrentlyInCreationException) {
					BeanCreationException bce = (BeanCreationException) rootCause;
					if (isCurrentlyInCreation(bce.getBeanName())) {
						if (this.logger.isDebugEnabled()) {
							this.logger.debug(
									"Ignoring match to currently created bean '" + beanName + "': " + ex.getMessage());
						}
						onSuppressedException(ex);
						// Ignore: indicates a circular reference when autowiring constructors.
						// We want to find matches other than the currently created bean itself.
						continue;
					}
				}
				throw ex;
			}
		}
		return result;
	}

	//获取Bean名称
	@Override
	public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
		List<String> results = new ArrayList<String>();
		for (String beanName : this.beanDefinitionNames) {
			BeanDefinition beanDefinition = getBeanDefinition(beanName);
			if (!beanDefinition.isAbstract() && findAnnotationOnBean(beanName, annotationType) != null) {
				results.add(beanName);
			}
		}
		for (String beanName : this.manualSingletonNames) {
			if (!results.contains(beanName) && findAnnotationOnBean(beanName, annotationType) != null) {
				results.add(beanName);
			}
		}
		return results.toArray(new String[results.size()]);
	}

	//获取Bean名称
	@Override
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
		String[] beanNames = getBeanNamesForAnnotation(annotationType);
		Map<String, Object> results = new LinkedHashMap<String, Object>(beanNames.length);
		for (String beanName : beanNames) {
			results.put(beanName, getBean(beanName));
		}
		return results;
	}

	//发现Bean中的注解
	@Override
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException {
		A ann = null;
		Class<?> beanType = getType(beanName);
		if (beanType != null) {
			ann = AnnotationUtils.findAnnotation(beanType, annotationType);
		}
		if (ann == null && containsBeanDefinition(beanName)) {
			BeanDefinition bd = getMergedBeanDefinition(beanName);
			if (bd instanceof AbstractBeanDefinition) {
				AbstractBeanDefinition abd = (AbstractBeanDefinition) bd;
				if (abd.hasBeanClass()) {
					ann = AnnotationUtils.findAnnotation(abd.getBeanClass(), annotationType);
				}
			}
		}
		return ann;
	}

	// ---------------------------------------------------------------------
	// 实现ConfigurableListableBeanFactory接口方法
	// ---------------------------------------------------------------------

	//注册可解析的依赖
	@Override
	public void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue) {
		Assert.notNull(dependencyType, "Dependency type must not be null");
		if (autowiredValue != null) {
			if (!(autowiredValue instanceof ObjectFactory || dependencyType.isInstance(autowiredValue))) {
				throw new IllegalArgumentException("Value [" + autowiredValue
						+ "] does not implement specified dependency type [" + dependencyType.getName() + "]");
			}
			this.resolvableDependencies.put(dependencyType, autowiredValue);
		}
	}

	//是否是自动装配候选
	@Override
	public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
			throws NoSuchBeanDefinitionException {
		return isAutowireCandidate(beanName, descriptor, getAutowireCandidateResolver());
	}

	//是否是自动装配候选者
	protected boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor,
			org.springframework.beans.factory.support.autowire.AutowireCandidateResolver resolver) throws NoSuchBeanDefinitionException {
		String beanDefinitionName = BeanFactoryUtils.transformedBeanName(beanName);
		if (containsBeanDefinition(beanDefinitionName)) {
			return isAutowireCandidate(beanName, getMergedLocalBeanDefinition(beanDefinitionName), descriptor,
					resolver);
		} else if (containsSingleton(beanName)) {
			return isAutowireCandidate(beanName, new RootBeanDefinition(getType(beanName)), descriptor, resolver);
		}

		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof DefaultListableBeanFactory) {
			// No bean definition found in this factory -> delegate to parent.
			return ((DefaultListableBeanFactory) parent).isAutowireCandidate(beanName, descriptor, resolver);
		} else if (parent instanceof ConfigurableListableBeanFactory) {
			// If no DefaultListableBeanFactory, can't pass the resolver along.
			return ((ConfigurableListableBeanFactory) parent).isAutowireCandidate(beanName, descriptor);
		} else {
			return true;
		}
	}

	//是否是自动装配候选者
	protected boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd, DependencyDescriptor descriptor,
										  org.springframework.beans.factory.support.autowire.AutowireCandidateResolver resolver) {
		String beanDefinitionName = BeanFactoryUtils.transformedBeanName(beanName);
		resolveBeanClass(mbd, beanDefinitionName);
		if (mbd.isFactoryMethodUnique) {
			boolean resolve;
			synchronized (mbd.constructorArgumentLock) {
				resolve = (mbd.resolvedConstructorOrFactoryMethod == null);
			}
			if (resolve) {
				new ConstructorResolver(this).resolveFactoryMethodIfPossible(mbd);
			}
		}
		return resolver.isAutowireCandidate(new BeanDefinitionHolder(mbd, beanName, getAliases(beanDefinitionName)), descriptor);
	}

	//获取Bean定义
	@Override
	public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		BeanDefinition bd = this.beanDefinitionMap.get(beanName);
		if (bd == null) {
			if (this.logger.isTraceEnabled()) {
				this.logger.trace("No bean named '" + beanName + "' found in " + this);
			}
			throw new NoSuchBeanDefinitionException(beanName);
		}
		return bd;
	}

	//获取Bean定义迭代器
	@Override
	public Iterator<String> getBeanNamesIterator() {
		CompositeIterator<String> iterator = new CompositeIterator<String>();
		iterator.add(this.beanDefinitionNames.iterator());
		iterator.add(this.manualSingletonNames.iterator());
		return iterator;
	}

	//清空元数据缓存
	@Override
	public void clearMetadataCache() {
		super.clearMetadataCache();
		clearByTypeCache();
	}

	//冻结配置信息
	@Override
	public void freezeConfiguration() {
		this.configurationFrozen = true;
		this.frozenBeanDefinitionNames = StringUtils.toStringArray(this.beanDefinitionNames);
	}

	//是否配置信息被冻结
	@Override
	public boolean isConfigurationFrozen() {
		return this.configurationFrozen;
	}

	/**
	 * Considers all beans as eligible for metadata caching if the factory's
	 * configuration has been marked as frozen.
	 * 
	 * @see #freezeConfiguration()
	 */
	@Override
	protected boolean isBeanEligibleForMetadataCaching(String beanName) {
		return (this.configurationFrozen || super.isBeanEligibleForMetadataCaching(beanName));
	}

	//提前实例化单例Bean
	@Override
	public void preInstantiateSingletons() throws BeansException {
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Pre-instantiating singletons in " + this);
		}

		// Iterate over a copy to allow for init methods which in turn register new bean
		// definitions.
		// While this may not be part of the regular factory bootstrap, it does
		// otherwise work fine.
		List<String> beanNames = new ArrayList<String>(this.beanDefinitionNames);

		// Trigger initialization of all non-lazy singleton beans...
		for (String beanName : beanNames) {
			RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
			if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
				if (isFactoryBean(beanName)) {
					final FactoryBean<?> factory = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
					boolean isEagerInit;
					if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
						isEagerInit = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
							@Override
							public Boolean run() {
								return ((SmartFactoryBean<?>) factory).isEagerInit();
							}
						}, getAccessControlContext());
					} else {
						isEagerInit = (factory instanceof SmartFactoryBean
								&& ((SmartFactoryBean<?>) factory).isEagerInit());
					}
					if (isEagerInit) {
						getBean(beanName);
					}
				} else {
					getBean(beanName);
				}
			}
		}

		// Trigger post-initialization callback for all applicable beans...
		for (String beanName : beanNames) {
			Object singletonInstance = getSingleton(beanName);
			if (singletonInstance instanceof SmartInitializingSingleton) {
				final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
				if (System.getSecurityManager() != null) {
					AccessController.doPrivileged(new PrivilegedAction<Object>() {
						@Override
						public Object run() {
							smartSingleton.afterSingletonsInstantiated();
							return null;
						}
					}, getAccessControlContext());
				} else {
					smartSingleton.afterSingletonsInstantiated();
				}
			}
		}
	}

	// ---------------------------------------------------------------------
	// 实现BeanDefinitionRegistry接口
	// ---------------------------------------------------------------------

	//注册Bean定义
	@Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {

		Assert.hasText(beanName, "Bean name must not be empty");
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");

		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				//对Bean定义进行验证
				((AbstractBeanDefinition) beanDefinition).validate();
			} catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
						"Validation of bean definition failed", ex);
			}
		}

		BeanDefinition oldBeanDefinition;
		//根据名称从缓存中获取Bean定义
		oldBeanDefinition = this.beanDefinitionMap.get(beanName);
		//若缓存已存在Bean定义
		if (oldBeanDefinition != null) {
			//若不允许Bean定义覆盖，则抛出异常
			if (!isAllowBeanDefinitionOverriding()) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
						"Cannot register bean definition [" + beanDefinition + "] for bean '" + beanName
								+ "': There is already [" + oldBeanDefinition + "] bound.");
			//若旧Bean定义角色小于新Bean定义角色
			} else if (oldBeanDefinition.getRole() < beanDefinition.getRole()) {
				if (this.logger.isWarnEnabled()) {
					this.logger.warn("Overriding user-defined bean definition for bean '" + beanName
							+ "' with a framework-generated bean definition: replacing [" + oldBeanDefinition
							+ "] with [" + beanDefinition + "]");
				}
			//若新Bean定义不等于旧Bean定义
			} else if (!beanDefinition.equals(oldBeanDefinition)) {
				if (this.logger.isInfoEnabled()) {
					this.logger.info("Overriding bean definition for bean '" + beanName
							+ "' with a different definition: replacing [" + oldBeanDefinition + "] with ["
							+ beanDefinition + "]");
				}
			//其他情况
			} else {
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("Overriding bean definition for bean '" + beanName
							+ "' with an equivalent definition: replacing [" + oldBeanDefinition + "] with ["
							+ beanDefinition + "]");
				}
			}
			//最后，用新Bean定义覆盖旧Bean定义
			this.beanDefinitionMap.put(beanName, beanDefinition);
		//若缓存不存在Bean定义
		} else {
			//是否已经有创建好的Bean
			if (hasBeanCreationStarted()) {
				// Cannot modify startup-time collection elements anymore (for stable iteration)
				synchronized (this.beanDefinitionMap) {
					//将Bean定义放入缓存中
					this.beanDefinitionMap.put(beanName, beanDefinition);
					//更新Bean定义名称集合
					List<String> updatedDefinitions = new ArrayList<String>(this.beanDefinitionNames.size() + 1);
					updatedDefinitions.addAll(this.beanDefinitionNames);
					updatedDefinitions.add(beanName);
					this.beanDefinitionNames = updatedDefinitions;
					if (this.manualSingletonNames.contains(beanName)) {
						Set<String> updatedSingletons = new LinkedHashSet<String>(this.manualSingletonNames);
						updatedSingletons.remove(beanName);
						this.manualSingletonNames = updatedSingletons;
					}
				}
			} else {
				this.beanDefinitionMap.put(beanName, beanDefinition);
				this.beanDefinitionNames.add(beanName);
				this.manualSingletonNames.remove(beanName);
			}
			this.frozenBeanDefinitionNames = null;
		}

		if (oldBeanDefinition != null || containsSingleton(beanName)) {
			resetBeanDefinition(beanName);
		}
	}

	//移除Bean定义
	@Override
	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		Assert.hasText(beanName, "'beanName' must not be empty");

		BeanDefinition bd = this.beanDefinitionMap.remove(beanName);
		if (bd == null) {
			if (this.logger.isTraceEnabled()) {
				this.logger.trace("No bean named '" + beanName + "' found in " + this);
			}
			throw new NoSuchBeanDefinitionException(beanName);
		}

		if (hasBeanCreationStarted()) {
			// Cannot modify startup-time collection elements anymore (for stable iteration)
			synchronized (this.beanDefinitionMap) {
				List<String> updatedDefinitions = new ArrayList<String>(this.beanDefinitionNames);
				updatedDefinitions.remove(beanName);
				this.beanDefinitionNames = updatedDefinitions;
			}
		} else {
			//仍处于启动注册阶段
			this.beanDefinitionNames.remove(beanName);
		}
		this.frozenBeanDefinitionNames = null;

		resetBeanDefinition(beanName);
	}

	//重置Bean定义
	protected void resetBeanDefinition(String beanName) {
		//清空合并的Bean定义
		clearMergedBeanDefinition(beanName);
		//销毁该Bean单例
		destroySingleton(beanName);

		// Reset all bean definitions that have the given bean as parent (recursively).
		for (String bdName : this.beanDefinitionNames) {
			if (!beanName.equals(bdName)) {
				BeanDefinition bd = this.beanDefinitionMap.get(bdName);
				if (beanName.equals(bd.getParentName())) {
					resetBeanDefinition(bdName);
				}
			}
		}
	}

	//是否允许覆盖别名
	@Override
	protected boolean allowAliasOverriding() {
		return isAllowBeanDefinitionOverriding();
	}

	//注册单例
	@Override
	public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
		super.registerSingleton(beanName, singletonObject);

		if (hasBeanCreationStarted()) {
			//无法再修改启动时的集合元素，因此直接替换集合
			synchronized (this.beanDefinitionMap) {
				if (!this.beanDefinitionMap.containsKey(beanName)) {
					Set<String> updatedSingletons = new LinkedHashSet<String>(this.manualSingletonNames.size() + 1);
					updatedSingletons.addAll(this.manualSingletonNames);
					updatedSingletons.add(beanName);
					this.manualSingletonNames = updatedSingletons;
				}
			}
		} else {
			//仍处于启动注册阶段
			if (!this.beanDefinitionMap.containsKey(beanName)) {
				this.manualSingletonNames.add(beanName);
			}
		}

		clearByTypeCache();
	}

	//销毁指定单例
	@Override
	public void destroySingleton(String beanName) {
		super.destroySingleton(beanName);
		this.manualSingletonNames.remove(beanName);
		clearByTypeCache();
	}

	//销毁所有单例
	@Override
	public void destroySingletons() {
		super.destroySingletons();
		this.manualSingletonNames.clear();
		clearByTypeCache();
	}

	//清空类型缓存
	private void clearByTypeCache() {
		this.allBeanNamesByType.clear();
		this.singletonBeanNamesByType.clear();
	}

	// ---------------------------------------------------------------------
	// 依赖解析支持方法
	// ---------------------------------------------------------------------

	//解析命名Bean
	@Override
	public <T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException {
		NamedBeanHolder<T> namedBean = resolveNamedBean(requiredType, (Object[]) null);
		if (namedBean != null) {
			return namedBean;
		}
		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof AutowireCapableBeanFactory) {
			return ((AutowireCapableBeanFactory) parent).resolveNamedBean(requiredType);
		}
		throw new NoSuchBeanDefinitionException(requiredType);
	}

	//解析命名Bean
	@SuppressWarnings("unchecked")
	private <T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType, Object... args) throws BeansException {
		Assert.notNull(requiredType, "Required type must not be null");
		//根据类型获取候选Bean名称
		String[] candidateNames = getBeanNamesForType(requiredType);

		if (candidateNames.length > 1) {
			List<String> autowireCandidates = new ArrayList<String>(candidateNames.length);
			//遍历多个候选Bean名称
			for (String beanName : candidateNames) {
				if (!containsBeanDefinition(beanName) || getBeanDefinition(beanName).isAutowireCandidate()) {
					autowireCandidates.add(beanName);
				}
			}
			if (!autowireCandidates.isEmpty()) {
				candidateNames = autowireCandidates.toArray(new String[autowireCandidates.size()]);
			}
		}
		//若候选Bean名称唯一
		if (candidateNames.length == 1) {
			//获取Bean名称
			String beanName = candidateNames[0];
			//直接获取Bean，并返回命名Bean持有器
			return new NamedBeanHolder<T>(beanName, getBean(beanName, requiredType, args));
		//若候选Bean名称不唯一
		} else if (candidateNames.length > 1) {
			Map<String, Object> candidates = new LinkedHashMap<String, Object>(candidateNames.length);
			//遍历候选Bean名称数组
			for (String beanName : candidateNames) {
				if (containsSingleton(beanName)) {
					candidates.put(beanName, getBean(beanName, requiredType, args));
				} else {
					candidates.put(beanName, getType(beanName));
				}
			}
			//确定主要候选者
			String candidateName = determinePrimaryCandidate(candidates, requiredType);
			//若主要候选者为空，则按最高优先级获取候选者
			if (candidateName == null) {
				candidateName = determineHighestPriorityCandidate(candidates, requiredType);
			}
			if (candidateName != null) {
				//获取候选Bean名称获取对应实例
				Object beanInstance = candidates.get(candidateName);
				if (beanInstance instanceof Class) {
					beanInstance = getBean(candidateName, requiredType, args);
				}
				return new NamedBeanHolder<T>(candidateName, (T) beanInstance);
			}
			throw new NoUniqueBeanDefinitionException(requiredType, candidates.keySet());
		}

		return null;
	}

	//依赖解析方法
	@Override
	public Object resolveDependency(DependencyDescriptor descriptor, String requestingBeanName,
			Set<String> autowiredBeanNames, TypeConverter typeConverter) throws BeansException {

		descriptor.initParameterNameDiscovery(getParameterNameDiscoverer());
		if (javaUtilOptionalClass == descriptor.getDependencyType()) {
			return new OptionalDependencyFactory().createOptionalDependency(descriptor, requestingBeanName);
		} else if (ObjectFactory.class == descriptor.getDependencyType()
				|| ObjectProvider.class == descriptor.getDependencyType()) {
			return new DependencyObjectProvider(descriptor, requestingBeanName);
		} else if (javaxInjectProviderClass == descriptor.getDependencyType()) {
			return new Jsr330ProviderFactory().createDependencyProvider(descriptor, requestingBeanName);
		} else {
			Object result = getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(descriptor,
					requestingBeanName);
			if (result == null) {
				result = doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames, typeConverter);
			}
			return result;
		}
	}

	//核心依赖解析方法
	public Object doResolveDependency(DependencyDescriptor descriptor, String beanName, Set<String> autowiredBeanNames,
			TypeConverter typeConverter) throws BeansException {

		InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
		try {
			Object shortcut = descriptor.resolveShortcut(this);
			if (shortcut != null) {
				return shortcut;
			}

			Class<?> type = descriptor.getDependencyType();
			Object value = getAutowireCandidateResolver().getSuggestedValue(descriptor);
			if (value != null) {
				if (value instanceof String) {
					String strVal = resolveEmbeddedValue((String) value);
					BeanDefinition bd = (beanName != null && containsBean(beanName) ? getMergedBeanDefinition(beanName)
							: null);
					value = evaluateBeanDefinitionString(strVal, bd);
				}
				TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
				return (descriptor.getField() != null ? converter.convertIfNecessary(value, type, descriptor.getField())
						: converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
			}

			Object multipleBeans = resolveMultipleBeans(descriptor, beanName, autowiredBeanNames, typeConverter);
			if (multipleBeans != null) {
				return multipleBeans;
			}

			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
			if (matchingBeans.isEmpty()) {
				if (isRequired(descriptor)) {
					raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
				}
				return null;
			}

			String autowiredBeanName;
			Object instanceCandidate;

			if (matchingBeans.size() > 1) {
				autowiredBeanName = determineAutowireCandidate(matchingBeans, descriptor);
				if (autowiredBeanName == null) {
					if (isRequired(descriptor) || !indicatesMultipleBeans(type)) {
						return descriptor.resolveNotUnique(type, matchingBeans);
					} else {
						// In case of an optional Collection/Map, silently ignore a non-unique case:
						// possibly it was meant to be an empty collection of multiple regular beans
						// (before 4.3 in particular when we didn't even look for collection beans).
						return null;
					}
				}
				instanceCandidate = matchingBeans.get(autowiredBeanName);
			} else {
				// We have exactly one match.
				Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
				autowiredBeanName = entry.getKey();
				instanceCandidate = entry.getValue();
			}

			if (autowiredBeanNames != null) {
				autowiredBeanNames.add(autowiredBeanName);
			}
			return (instanceCandidate instanceof Class ? descriptor.resolveCandidate(autowiredBeanName, type, this)
					: instanceCandidate);
		} finally {
			ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
		}
	}

	//解析多元Bean实例
	private Object resolveMultipleBeans(DependencyDescriptor descriptor, String beanName,
			Set<String> autowiredBeanNames, TypeConverter typeConverter) {

		Class<?> type = descriptor.getDependencyType();
		if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			ResolvableType resolvableType = descriptor.getResolvableType();
			Class<?> resolvedArrayType = resolvableType.resolve();
			if (resolvedArrayType != null && resolvedArrayType != type) {
				type = resolvedArrayType;
				componentType = resolvableType.getComponentType().resolve();
			}
			if (componentType == null) {
				return null;
			}
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, componentType,
					new MultiElementDescriptor(descriptor));
			if (matchingBeans.isEmpty()) {
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
			Object result = converter.convertIfNecessary(matchingBeans.values(), type);
			if (getDependencyComparator() != null && result instanceof Object[]) {
				Arrays.sort((Object[]) result, adaptDependencyComparator(matchingBeans));
			}
			return result;
		} else if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
			Class<?> elementType = descriptor.getResolvableType().asCollection().resolveGeneric();
			if (elementType == null) {
				return null;
			}
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, elementType,
					new MultiElementDescriptor(descriptor));
			if (matchingBeans.isEmpty()) {
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
			Object result = converter.convertIfNecessary(matchingBeans.values(), type);
			if (getDependencyComparator() != null && result instanceof List) {
				Collections.sort((List<?>) result, adaptDependencyComparator(matchingBeans));
			}
			return result;
		} else if (Map.class == type) {
			ResolvableType mapType = descriptor.getResolvableType().asMap();
			Class<?> keyType = mapType.resolveGeneric(0);
			if (String.class != keyType) {
				return null;
			}
			Class<?> valueType = mapType.resolveGeneric(1);
			if (valueType == null) {
				return null;
			}
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, valueType,
					new MultiElementDescriptor(descriptor));
			if (matchingBeans.isEmpty()) {
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			return matchingBeans;
		} else {
			return null;
		}
	}

	private boolean isRequired(DependencyDescriptor descriptor) {
		AutowireCandidateResolver resolver = getAutowireCandidateResolver();
		return (resolver instanceof org.springframework.beans.factory.support.autowire.SimpleAutowireCandidateResolver
				? ((SimpleAutowireCandidateResolver) resolver).isRequired(descriptor)
				: descriptor.isRequired());
	}

	private boolean indicatesMultipleBeans(Class<?> type) {
		return (type.isArray() || (type.isInterface()
				&& (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type))));
	}

	private Comparator<Object> adaptDependencyComparator(Map<String, Object> matchingBeans) {
		Comparator<Object> comparator = getDependencyComparator();
		if (comparator instanceof OrderComparator) {
			return ((OrderComparator) comparator)
					.withSourceProvider(createFactoryAwareOrderSourceProvider(matchingBeans));
		} else {
			return comparator;
		}
	}

	//创建工厂装配顺序源提供者
	private FactoryAwareOrderSourceProvider createFactoryAwareOrderSourceProvider(Map<String, Object> beans) {
		IdentityHashMap<Object, String> instancesToBeanNames = new IdentityHashMap<Object, String>();
		for (Map.Entry<String, Object> entry : beans.entrySet()) {
			instancesToBeanNames.put(entry.getValue(), entry.getKey());
		}
		return new FactoryAwareOrderSourceProvider(instancesToBeanNames);
	}

	//发现自动转配候选者
	protected Map<String, Object> findAutowireCandidates(String beanName, Class<?> requiredType,
			DependencyDescriptor descriptor) {

		String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, requiredType, true,
				descriptor.isEager());
		Map<String, Object> result = new LinkedHashMap<String, Object>(candidateNames.length);
		for (Class<?> autowiringType : this.resolvableDependencies.keySet()) {
			if (autowiringType.isAssignableFrom(requiredType)) {
				Object autowiringValue = this.resolvableDependencies.get(autowiringType);
				autowiringValue = AutowireUtils.resolveAutowiringValue(autowiringValue, requiredType);
				if (requiredType.isInstance(autowiringValue)) {
					result.put(ObjectUtils.identityToString(autowiringValue), autowiringValue);
					break;
				}
			}
		}
		for (String candidate : candidateNames) {
			if (!isSelfReference(beanName, candidate) && isAutowireCandidate(candidate, descriptor)) {
				addCandidateEntry(result, candidate, descriptor, requiredType);
			}
		}
		if (result.isEmpty() && !indicatesMultipleBeans(requiredType)) {
			// Consider fallback matches if the first pass failed to find anything...
			DependencyDescriptor fallbackDescriptor = descriptor.forFallbackMatch();
			for (String candidate : candidateNames) {
				if (!isSelfReference(beanName, candidate) && isAutowireCandidate(candidate, fallbackDescriptor)) {
					addCandidateEntry(result, candidate, descriptor, requiredType);
				}
			}
			if (result.isEmpty()) {
				// Consider self references as a final pass...
				// but in the case of a dependency collection, not the very same bean itself.
				for (String candidate : candidateNames) {
					if (isSelfReference(beanName, candidate)
							&& (!(descriptor instanceof MultiElementDescriptor) || !beanName.equals(candidate))
							&& isAutowireCandidate(candidate, fallbackDescriptor)) {
						addCandidateEntry(result, candidate, descriptor, requiredType);
					}
				}
			}
		}
		return result;
	}

	//添加候选者实体
	private void addCandidateEntry(Map<String, Object> candidates, String candidateName,
			DependencyDescriptor descriptor, Class<?> requiredType) {

		if (descriptor instanceof MultiElementDescriptor || containsSingleton(candidateName)) {
			candidates.put(candidateName, descriptor.resolveCandidate(candidateName, requiredType, this));
		} else {
			candidates.put(candidateName, getType(candidateName));
		}
	}

	//确定自动装配候选者
	protected String determineAutowireCandidate(Map<String, Object> candidates, DependencyDescriptor descriptor) {
		Class<?> requiredType = descriptor.getDependencyType();
		String primaryCandidate = determinePrimaryCandidate(candidates, requiredType);
		if (primaryCandidate != null) {
			return primaryCandidate;
		}
		String priorityCandidate = determineHighestPriorityCandidate(candidates, requiredType);
		if (priorityCandidate != null) {
			return priorityCandidate;
		}
		// Fallback
		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			String candidateName = entry.getKey();
			Object beanInstance = entry.getValue();
			if ((beanInstance != null && this.resolvableDependencies.containsValue(beanInstance))
					|| matchesBeanName(candidateName, descriptor.getDependencyName())) {
				return candidateName;
			}
		}
		return null;
	}

	//确定主要候选者
	protected String determinePrimaryCandidate(Map<String, Object> candidates, Class<?> requiredType) {
		String primaryBeanName = null;
		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			String candidateBeanName = entry.getKey();
			Object beanInstance = entry.getValue();
			if (isPrimary(candidateBeanName, beanInstance)) {
				if (primaryBeanName != null) {
					boolean candidateLocal = containsBeanDefinition(candidateBeanName);
					boolean primaryLocal = containsBeanDefinition(primaryBeanName);
					if (candidateLocal && primaryLocal) {
						throw new NoUniqueBeanDefinitionException(requiredType, candidates.size(),
								"more than one 'primary' bean found among candidates: " + candidates.keySet());
					} else if (candidateLocal) {
						primaryBeanName = candidateBeanName;
					}
				} else {
					primaryBeanName = candidateBeanName;
				}
			}
		}
		return primaryBeanName;
	}

	//确定最高优先级的候选者
	protected String determineHighestPriorityCandidate(Map<String, Object> candidates, Class<?> requiredType) {
		String highestPriorityBeanName = null;
		Integer highestPriority = null;
		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			String candidateBeanName = entry.getKey();
			Object beanInstance = entry.getValue();
			Integer candidatePriority = getPriority(beanInstance);
			if (candidatePriority != null) {
				if (highestPriorityBeanName != null) {
					if (candidatePriority.equals(highestPriority)) {
						throw new NoUniqueBeanDefinitionException(requiredType, candidates.size(),
								"Multiple beans found with the same priority ('" + highestPriority
										+ "') among candidates: " + candidates.keySet());
					} else if (candidatePriority < highestPriority) {
						highestPriorityBeanName = candidateBeanName;
						highestPriority = candidatePriority;
					}
				} else {
					highestPriorityBeanName = candidateBeanName;
					highestPriority = candidatePriority;
				}
			}
		}
		return highestPriorityBeanName;
	}

	//是否是主要的Bean
	protected boolean isPrimary(String beanName, Object beanInstance) {
		if (containsBeanDefinition(beanName)) {
			return getMergedLocalBeanDefinition(beanName).isPrimary();
		}
		BeanFactory parent = getParentBeanFactory();
		return (parent instanceof DefaultListableBeanFactory
				&& ((DefaultListableBeanFactory) parent).isPrimary(beanName, beanInstance));
	}

	//获取优先级
	protected Integer getPriority(Object beanInstance) {
		Comparator<Object> comparator = getDependencyComparator();
		if (comparator instanceof OrderComparator) {
			return ((OrderComparator) comparator).getPriority(beanInstance);
		}
		return null;
	}

	//匹配Bean的名称
	protected boolean matchesBeanName(String beanName, String candidateName) {
		return (candidateName != null && (candidateName.equals(beanName)
				|| ObjectUtils.containsElement(getAliases(beanName), candidateName)));
	}

	//是否自我引用
	private boolean isSelfReference(String beanName, String candidateName) {
		return (beanName != null && candidateName != null
				&& (beanName.equals(candidateName) || (containsBeanDefinition(candidateName)
						&& beanName.equals(getMergedLocalBeanDefinition(candidateName).getFactoryBeanName()))));
	}

	/**
	 * Raise a NoSuchBeanDefinitionException or BeanNotOfRequiredTypeException for
	 * an unresolvable dependency.
	 */
	private void raiseNoMatchingBeanFound(Class<?> type, ResolvableType resolvableType, DependencyDescriptor descriptor)
			throws BeansException {

		checkBeanNotOfRequiredType(type, descriptor);

		throw new NoSuchBeanDefinitionException(resolvableType,
				"expected at least 1 bean which qualifies as autowire candidate. " + "Dependency annotations: "
						+ ObjectUtils.nullSafeToString(descriptor.getAnnotations()));
	}

	/**
	 * Raise a BeanNotOfRequiredTypeException for an unresolvable dependency, if
	 * applicable, i.e. if the target type of the bean would match but an exposed
	 * proxy doesn't.
	 */
	private void checkBeanNotOfRequiredType(Class<?> type, DependencyDescriptor descriptor) {
		for (String beanName : this.beanDefinitionNames) {
			RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
			Class<?> targetType = mbd.getTargetType();
			if (targetType != null && type.isAssignableFrom(targetType)
					&& isAutowireCandidate(beanName, mbd, descriptor, getAutowireCandidateResolver())) {
				// Probably a proxy interfering with target type match -> throw meaningful
				// exception.
				Object beanInstance = getSingleton(beanName, false);
				Class<?> beanType = (beanInstance != null ? beanInstance.getClass() : predictBeanType(beanName, mbd));
				if (!type.isAssignableFrom((beanType))) {
					throw new BeanNotOfRequiredTypeException(beanName, type, beanType);
				}
			}
		}

		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof DefaultListableBeanFactory) {
			((DefaultListableBeanFactory) parent).checkBeanNotOfRequiredType(type, descriptor);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(ObjectUtils.identityToString(this));
		sb.append(": defining beans [");
		sb.append(StringUtils.collectionToCommaDelimitedString(this.beanDefinitionNames));
		sb.append("]; ");
		BeanFactory parent = getParentBeanFactory();
		if (parent == null) {
			sb.append("root of factory hierarchy");
		} else {
			sb.append("parent: ").append(ObjectUtils.identityToString(parent));
		}
		return sb.toString();
	}

	// ---------------------------------------------------------------------
	// 序列化支持方法
	// ---------------------------------------------------------------------

	//读取对象
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		throw new NotSerializableException("DefaultListableBeanFactory itself is not deserializable - "
				+ "just a SerializedBeanFactoryReference is");
	}

	//写入对象
	protected Object writeReplace() throws ObjectStreamException {
		if (this.serializationId != null) {
			return new SerializedBeanFactoryReference(this.serializationId);
		} else {
			throw new NotSerializableException("DefaultListableBeanFactory has no serialization id");
		}
	}

	//序列化的Bean工厂引用
	private static class SerializedBeanFactoryReference implements Serializable {

		private final String id;

		public SerializedBeanFactoryReference(String id) {
			this.id = id;
		}

		private Object readResolve() {
			Reference<?> ref = serializableFactories.get(this.id);
			if (ref != null) {
				Object result = ref.get();
				if (result != null) {
					return result;
				}
			}
			return new DefaultListableBeanFactory();
		}
	}

	@UsesJava8
	private class OptionalDependencyFactory {

		public Object createOptionalDependency(DependencyDescriptor descriptor, String beanName, final Object... args) {
			DependencyDescriptor descriptorToUse = new NestedDependencyDescriptor(descriptor) {
				@Override
				public boolean isRequired() {
					return false;
				}

				@Override
				public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory) {
					return (!ObjectUtils.isEmpty(args) ? beanFactory.getBean(beanName, requiredType, args)
							: super.resolveCandidate(beanName, requiredType, beanFactory));
				}
			};
			return Optional.ofNullable(doResolveDependency(descriptorToUse, beanName, null, null));
		}
	}

	//依赖对象提供者
	private class DependencyObjectProvider implements ObjectProvider<Object>, Serializable {

		private final DependencyDescriptor descriptor;

		private final boolean optional;

		private final String beanName;

		public DependencyObjectProvider(DependencyDescriptor descriptor, String beanName) {
			this.descriptor = new NestedDependencyDescriptor(descriptor);
			this.optional = (this.descriptor.getDependencyType() == javaUtilOptionalClass);
			this.beanName = beanName;
		}

		@Override
		public Object getObject() throws BeansException {
			if (this.optional) {
				return new OptionalDependencyFactory().createOptionalDependency(this.descriptor, this.beanName);
			} else {
				return doResolveDependency(this.descriptor, this.beanName, null, null);
			}
		}

		@Override
		public Object getObject(final Object... args) throws BeansException {
			if (this.optional) {
				return new OptionalDependencyFactory().createOptionalDependency(this.descriptor, this.beanName, args);
			} else {
				DependencyDescriptor descriptorToUse = new DependencyDescriptor(descriptor) {
					@Override
					public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory) {
						return ((AbstractBeanFactory) beanFactory).getBean(beanName, requiredType, args);
					}
				};
				return doResolveDependency(descriptorToUse, this.beanName, null, null);
			}
		}

		@Override
		public Object getIfAvailable() throws BeansException {
			if (this.optional) {
				return new OptionalDependencyFactory().createOptionalDependency(this.descriptor, this.beanName);
			} else {
				DependencyDescriptor descriptorToUse = new DependencyDescriptor(descriptor) {
					@Override
					public boolean isRequired() {
						return false;
					}
				};
				return doResolveDependency(descriptorToUse, this.beanName, null, null);
			}
		}

		@Override
		public Object getIfUnique() throws BeansException {
			DependencyDescriptor descriptorToUse = new DependencyDescriptor(descriptor) {
				@Override
				public boolean isRequired() {
					return false;
				}

				@Override
				public Object resolveNotUnique(Class<?> type, Map<String, Object> matchingBeans) {
					return null;
				}
			};
			if (this.optional) {
				return new OptionalDependencyFactory().createOptionalDependency(descriptorToUse, this.beanName);
			} else {
				return doResolveDependency(descriptorToUse, this.beanName, null, null);
			}
		}
	}

	//Jsr330依赖提供者
	private class Jsr330DependencyProvider extends DependencyObjectProvider implements Provider<Object> {

		public Jsr330DependencyProvider(DependencyDescriptor descriptor, String beanName) {
			super(descriptor, beanName);
		}

		@Override
		public Object get() throws BeansException {
			return getObject();
		}
	}

	//Jsr330依赖提供者工厂
	private class Jsr330ProviderFactory {
		public Object createDependencyProvider(DependencyDescriptor descriptor, String beanName) {
			return new Jsr330DependencyProvider(descriptor, beanName);
		}
	}

	//工厂装配顺序源提供者
	private class FactoryAwareOrderSourceProvider implements OrderComparator.OrderSourceProvider {

		private final Map<Object, String> instancesToBeanNames;

		public FactoryAwareOrderSourceProvider(Map<Object, String> instancesToBeanNames) {
			this.instancesToBeanNames = instancesToBeanNames;
		}

		@Override
		public Object getOrderSource(Object obj) {
			RootBeanDefinition beanDefinition = getRootBeanDefinition(this.instancesToBeanNames.get(obj));
			if (beanDefinition == null) {
				return null;
			}
			List<Object> sources = new ArrayList<Object>(2);
			Method factoryMethod = beanDefinition.getResolvedFactoryMethod();
			if (factoryMethod != null) {
				sources.add(factoryMethod);
			}
			Class<?> targetType = beanDefinition.getTargetType();
			if (targetType != null && targetType != obj.getClass()) {
				sources.add(targetType);
			}
			return sources.toArray(new Object[sources.size()]);
		}

		private RootBeanDefinition getRootBeanDefinition(String beanName) {
			if (beanName != null && containsBeanDefinition(beanName)) {
				BeanDefinition bd = getMergedBeanDefinition(beanName);
				if (bd instanceof RootBeanDefinition) {
					return (RootBeanDefinition) bd;
				}
			}
			return null;
		}
	}

	//嵌套依赖描述符
	private static class NestedDependencyDescriptor extends DependencyDescriptor {
		public NestedDependencyDescriptor(DependencyDescriptor original) {
			super(original);
			increaseNestingLevel();
		}
	}

	//多个元素描述符
	private static class MultiElementDescriptor extends NestedDependencyDescriptor {
		public MultiElementDescriptor(DependencyDescriptor original) {
			super(original);
		}
	}

}
