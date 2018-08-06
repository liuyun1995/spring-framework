package org.springframework.beans.factory.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.bean.BeanUtils;
import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.support.autowire.BeanClassLoaderAware;
import org.springframework.beans.factory.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.processor.BeanFactoryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

//外部范围配置器
public class CustomScopeConfigurer implements BeanFactoryPostProcessor, BeanClassLoaderAware, Ordered {

	private Map<String, Object> scopes;
	private int order = Ordered.LOWEST_PRECEDENCE;
	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	public void setScopes(Map<String, Object> scopes) {
		this.scopes = scopes;
	}

	public void addScope(String scopeName, Scope scope) {
		if (this.scopes == null) {
			this.scopes = new LinkedHashMap<String, Object>(1);
		}
		this.scopes.put(scopeName, scope);
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.scopes != null) {
			for (Map.Entry<String, Object> entry : this.scopes.entrySet()) {
				String scopeKey = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof Scope) {
					beanFactory.registerScope(scopeKey, (Scope) value);
				} else if (value instanceof Class) {
					Class<?> scopeClass = (Class<?>) value;
					Assert.isAssignable(Scope.class, scopeClass, "Invalid scope class");
					beanFactory.registerScope(scopeKey, (Scope) BeanUtils.instantiateClass(scopeClass));
				} else if (value instanceof String) {
					Class<?> scopeClass = ClassUtils.resolveClassName((String) value, this.beanClassLoader);
					Assert.isAssignable(Scope.class, scopeClass, "Invalid scope class");
					beanFactory.registerScope(scopeKey, (Scope) BeanUtils.instantiateClass(scopeClass));
				} else {
					throw new IllegalArgumentException("Mapped value [" + value + "] for scope key [" + scopeKey
							+ "] is not an instance of required type [" + Scope.class.getName()
							+ "] or a corresponding Class or String value indicating a Scope implementation");
				}
			}
		}
	}

}
