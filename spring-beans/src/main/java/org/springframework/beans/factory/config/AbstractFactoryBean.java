package org.springframework.beans.factory.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

//抽象工厂Bean
public abstract class AbstractFactoryBean<T>
		implements FactoryBean<T>, BeanClassLoaderAware, BeanFactoryAware, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private boolean singleton = true;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private BeanFactory beanFactory;

	private boolean initialized = false;

	private T singletonInstance;

	private T earlySingletonInstance;

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	@Override
	public boolean isSingleton() {
		return this.singleton;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	//获取Bean类型转换器
	protected TypeConverter getBeanTypeConverter() {
		BeanFactory beanFactory = getBeanFactory();
		if (beanFactory instanceof ConfigurableBeanFactory) {
			return ((ConfigurableBeanFactory) beanFactory).getTypeConverter();
		} else {
			return new SimpleTypeConverter();
		}
	}

	/**
	 * Eagerly create the singleton instance, if necessary.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (isSingleton()) {
			this.initialized = true;
			this.singletonInstance = createInstance();
			this.earlySingletonInstance = null;
		}
	}

	/**
	 * Expose the singleton instance or create a new prototype instance.
	 * 
	 * @see #createInstance()
	 * @see #getEarlySingletonInterfaces()
	 */
	@Override
	public final T getObject() throws Exception {
		if (isSingleton()) {
			return (this.initialized ? this.singletonInstance : getEarlySingletonInstance());
		} else {
			return createInstance();
		}
	}

	/**
	 * Determine an 'eager singleton' instance, exposed in case of a circular
	 * reference. Not called in a non-circular scenario.
	 */
	@SuppressWarnings("unchecked")
	private T getEarlySingletonInstance() throws Exception {
		Class<?>[] ifcs = getEarlySingletonInterfaces();
		if (ifcs == null) {
			throw new FactoryBeanNotInitializedException(
					getClass().getName() + " does not support circular references");
		}
		if (this.earlySingletonInstance == null) {
			this.earlySingletonInstance = (T) Proxy.newProxyInstance(this.beanClassLoader, ifcs,
					new EarlySingletonInvocationHandler());
		}
		return this.earlySingletonInstance;
	}

	/**
	 * Expose the singleton instance (for access through the 'early singleton'
	 * proxy).
	 * 
	 * @return the singleton instance that this FactoryBean holds
	 * @throws IllegalStateException
	 *             if the singleton instance is not initialized
	 */
	private T getSingletonInstance() throws IllegalStateException {
		if (!this.initialized) {
			throw new IllegalStateException("Singleton instance not initialized yet");
		}
		return this.singletonInstance;
	}
	
	@Override
	public void destroy() throws Exception {
		if (isSingleton()) {
			destroyInstance(this.singletonInstance);
		}
	}

	/**
	 * This abstract method declaration mirrors the method in the FactoryBean
	 * interface, for a consistent offering of abstract template methods.
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public abstract Class<?> getObjectType();
	
	protected abstract T createInstance() throws Exception;

	/**
	 * Return an array of interfaces that a singleton object exposed by this
	 * FactoryBean is supposed to implement, for use with an 'early singleton proxy'
	 * that will be exposed in case of a circular reference.
	 * <p>
	 * The default implementation returns this FactoryBean's object type, provided
	 * that it is an interface, or {@code null} else. The latter indicates that
	 * early singleton access is not supported by this FactoryBean. This will lead
	 * to a FactoryBeanNotInitializedException getting thrown.
	 * 
	 * @return the interfaces to use for 'early singletons', or {@code null} to
	 *         indicate a FactoryBeanNotInitializedException
	 * @see org.springframework.beans.factory.FactoryBeanNotInitializedException
	 */
	protected Class<?>[] getEarlySingletonInterfaces() {
		Class<?> type = getObjectType();
		return (type != null && type.isInterface() ? new Class<?>[] { type } : null);
	}
	
	protected void destroyInstance(T instance) throws Exception {}
	
	private class EarlySingletonInvocationHandler implements InvocationHandler {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (ReflectionUtils.isEqualsMethod(method)) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0]);
			} else if (ReflectionUtils.isHashCodeMethod(method)) {
				// Use hashCode of reference proxy.
				return System.identityHashCode(proxy);
			} else if (!initialized && ReflectionUtils.isToStringMethod(method)) {
				return "Early singleton proxy for interfaces "
						+ ObjectUtils.nullSafeToString(getEarlySingletonInterfaces());
			}
			try {
				return method.invoke(getSingletonInstance(), args);
			} catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
