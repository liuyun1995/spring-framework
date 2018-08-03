package org.springframework.beans.factory.bean.factorybean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.bean.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.bean.DisposableBean;
import org.springframework.beans.factory.bean.factory.ConfigurableBeanFactory;
import org.springframework.beans.exception.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.bean.InitializingBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

//抽象工厂Bean
public abstract class AbstractFactoryBean<T>
		implements FactoryBean<T>, BeanClassLoaderAware, BeanFactoryAware, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());                  //日志类
	private boolean singleton = true;                                            //是否单例
	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();    //类加载器
	private BeanFactory beanFactory;                                             //Bean工厂
	private boolean initialized = false;                                         //是否初始化
	private T singletonInstance;                                                 //单实例
	private T earlySingletonInstance;                                            //早期单实例

	//设置单例
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	//是否单例
	@Override
	public boolean isSingleton() {
		return this.singleton;
	}

	//设置类加载器
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	//设置Bean工厂
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	//获取Bean工厂
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

	//属性值设置之后执行
	@Override
	public void afterPropertiesSet() throws Exception {
		if (isSingleton()) {
			this.initialized = true;
			this.singletonInstance = createInstance();
			this.earlySingletonInstance = null;
		}
	}

	//获取对象
	@Override
	public final T getObject() throws Exception {
		//判断是否是单例
		if (isSingleton()) {
			//若初始化过则返回单实例，否则返回早期单实例
			return (this.initialized ? this.singletonInstance : getEarlySingletonInstance());
		} else {
			//创建实例
			return createInstance();
		}
	}

	//获取早期单实例
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

	//获取单实例
	private T getSingletonInstance() throws IllegalStateException {
		if (!this.initialized) {
			throw new IllegalStateException("Singleton instance not initialized yet");
		}
		return this.singletonInstance;
	}

	//销毁方法
	@Override
	public void destroy() throws Exception {
		//判断是否是单例
		if (isSingleton()) {
			//销毁实例
			destroyInstance(this.singletonInstance);
		}
	}

	//获取对象类型
	@Override
	public abstract Class<?> getObjectType();

	//创建实例
	protected abstract T createInstance() throws Exception;

	//获取早期单例接口
	protected Class<?>[] getEarlySingletonInterfaces() {
		//获取对象类型
		Class<?> type = getObjectType();
		//返回Class数组
		return (type != null && type.isInterface() ? new Class<?>[] { type } : null);
	}

	//销毁实例
	protected void destroyInstance(T instance) throws Exception {}

	//早期单实例调用处理器
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
