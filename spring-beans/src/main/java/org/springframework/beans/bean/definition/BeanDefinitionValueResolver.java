package org.springframework.beans.bean.definition;

import org.springframework.beans.bean.BeanWrapper;
import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.support.merge.*;
import org.springframework.beans.property.type.TypeConverter;
import org.springframework.beans.factory.AbstractBeanFactory;
import org.springframework.beans.exception.BeanCreationException;
import org.springframework.beans.exception.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.bean.factorybean.FactoryBean;
import org.springframework.beans.factory.config.*;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import java.lang.reflect.Array;
import java.util.*;

//Bean定义值解析器
class BeanDefinitionValueResolver {

	private final AbstractBeanFactory beanFactory;   //抽象Bean工厂
	private final String beanName;                   //Bean的名称
	private final BeanDefinition beanDefinition;     //Bean定义
	private final TypeConverter typeConverter;       //类型转换器

	//构造器
	public BeanDefinitionValueResolver(AbstractBeanFactory beanFactory, String beanName, BeanDefinition beanDefinition,
                                       TypeConverter typeConverter) {
		this.beanFactory = beanFactory;
		this.beanName = beanName;
		this.beanDefinition = beanDefinition;
		this.typeConverter = typeConverter;
	}

	//解析值
	public Object resolveValueIfNecessary(Object argName, Object value) {
		//如果是RuntimeBeanReference实例
		if (value instanceof RuntimeBeanReference) {
			RuntimeBeanReference ref = (RuntimeBeanReference) value;
			return resolveReference(argName, ref);
		//如果是RuntimeBeanNameReference实例
		} else if (value instanceof RuntimeBeanNameReference) {
			String refName = ((RuntimeBeanNameReference) value).getBeanName();
			refName = String.valueOf(doEvaluate(refName));
			if (!this.beanFactory.containsBean(refName)) {
				throw new BeanDefinitionStoreException("Invalid bean name '" + refName + "' in bean reference for " + argName);
			}
			return refName;
		//如果是BeanDefinitionHolder实例
		} else if (value instanceof BeanDefinitionHolder) {
			BeanDefinitionHolder bdHolder = (BeanDefinitionHolder) value;
			return resolveInnerBean(argName, bdHolder.getBeanName(), bdHolder.getBeanDefinition());
		//如果是BeanDefinition实例
		} else if (value instanceof BeanDefinition) {
			BeanDefinition bd = (BeanDefinition) value;
			String innerBeanName = "(inner bean)" + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR
					+ ObjectUtils.getIdentityHexString(bd);
			return resolveInnerBean(argName, innerBeanName, bd);
		//如果是ManagedArray实例
		} else if (value instanceof ManagedArray) {
			ManagedArray array = (ManagedArray) value;
			Class<?> elementType = array.resolvedElementType;
			if (elementType == null) {
				String elementTypeName = array.getElementTypeName();
				if (StringUtils.hasText(elementTypeName)) {
					try {
						elementType = ClassUtils.forName(elementTypeName, this.beanFactory.getBeanClassLoader());
						array.resolvedElementType = elementType;
					} catch (Throwable ex) {
						// Improve the message by showing the context.
						throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName,
								"Error resolving array type for " + argName, ex);
					}
				} else {
					elementType = Object.class;
				}
			}
			return resolveManagedArray(argName, (List<?>) value, elementType);
		//如果是ManagedList实例
		} else if (value instanceof ManagedList) {
			return resolveManagedList(argName, (List<?>) value);
		//如果是ManagedSet实例
		} else if (value instanceof ManagedSet) {
			return resolveManagedSet(argName, (Set<?>) value);
		//如果是ManagedMap实例
		} else if (value instanceof ManagedMap) {
			return resolveManagedMap(argName, (Map<?, ?>) value);
		//如果是ManagedProperties实例
		} else if (value instanceof ManagedProperties) {
			Properties original = (Properties) value;
			Properties copy = new Properties();
			for (Map.Entry<Object, Object> propEntry : original.entrySet()) {
				Object propKey = propEntry.getKey();
				Object propValue = propEntry.getValue();
				if (propKey instanceof TypedStringValue) {
					propKey = evaluate((TypedStringValue) propKey);
				}
				if (propValue instanceof TypedStringValue) {
					propValue = evaluate((TypedStringValue) propValue);
				}
				copy.put(propKey, propValue);
			}
			return copy;
		//如果是TypedStringValue实例
		} else if (value instanceof TypedStringValue) {
			TypedStringValue typedStringValue = (TypedStringValue) value;
			Object valueObject = evaluate(typedStringValue);
			try {
				Class<?> resolvedTargetType = resolveTargetType(typedStringValue);
				if (resolvedTargetType != null) {
					return this.typeConverter.convertIfNecessary(valueObject, resolvedTargetType);
				} else {
					return valueObject;
				}
			} catch (Throwable ex) {
				// Improve the message by showing the context.
				throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName,
						"Error converting typed String value for " + argName, ex);
			}
		} else {
			return evaluate(value);
		}
	}


	protected Object evaluate(TypedStringValue value) {
		Object result = doEvaluate(value.getValue());
		if (!ObjectUtils.nullSafeEquals(result, value.getValue())) {
			value.setDynamic();
		}
		return result;
	}

	protected Object evaluate(Object value) {
		if (value instanceof String) {
			return doEvaluate((String) value);
		} else if (value instanceof String[]) {
			String[] values = (String[]) value;
			boolean actuallyResolved = false;
			Object[] resolvedValues = new Object[values.length];
			for (int i = 0; i < values.length; i++) {
				String originalValue = values[i];
				Object resolvedValue = doEvaluate(originalValue);
				if (resolvedValue != originalValue) {
					actuallyResolved = true;
				}
				resolvedValues[i] = resolvedValue;
			}
			return (actuallyResolved ? resolvedValues : values);
		} else {
			return value;
		}
	}

	private Object doEvaluate(String value) {
		return this.beanFactory.evaluateBeanDefinitionString(value, this.beanDefinition);
	}

	/**
	 * Resolve the target type in the given TypedStringValue.
	 * 
	 * @param value
	 *            the TypedStringValue to resolve
	 * @return the resolved target type (or {@code null} if none specified)
	 * @throws ClassNotFoundException
	 *             if the specified type cannot be resolved
	 * @see TypedStringValue#resolveTargetType
	 */
	protected Class<?> resolveTargetType(TypedStringValue value) throws ClassNotFoundException {
		if (value.hasTargetType()) {
			return value.getTargetType();
		}
		return value.resolveTargetType(this.beanFactory.getBeanClassLoader());
	}

	//解析内部Bean
	private Object resolveInnerBean(Object argName, String innerBeanName, BeanDefinition innerBd) {
		RootBeanDefinition mbd = null;
		try {
			mbd = this.beanFactory.getMergedBeanDefinition(innerBeanName, innerBd, this.beanDefinition);
			// Check given bean name whether it is unique. If not already unique,
			// add counter - increasing the counter until the name is unique.
			String actualInnerBeanName = innerBeanName;
			if (mbd.isSingleton()) {
				actualInnerBeanName = adaptInnerBeanName(innerBeanName);
			}
			this.beanFactory.registerContainedBean(actualInnerBeanName, this.beanName);
			// Guarantee initialization of beans that the inner bean depends on.
			String[] dependsOn = mbd.getDependsOn();
			if (dependsOn != null) {
				for (String dependsOnBean : dependsOn) {
					this.beanFactory.registerDependentBean(dependsOnBean, actualInnerBeanName);
					this.beanFactory.getBean(dependsOnBean);
				}
			}
			// Actually create the inner bean instance now...
			Object innerBean = this.beanFactory.createBean(actualInnerBeanName, mbd, null);
			if (innerBean instanceof FactoryBean) {
				boolean synthetic = mbd.isSynthetic();
				return this.beanFactory.getObjectFromFactoryBean((FactoryBean<?>) innerBean, actualInnerBeanName,
						!synthetic);
			} else {
				return innerBean;
			}
		} catch (BeansException ex) {
			throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName,
					"Cannot create inner bean '" + innerBeanName + "' "
							+ (mbd != null && mbd.getBeanClassName() != null
									? "of type [" + mbd.getBeanClassName() + "] "
									: "")
							+ "while setting " + argName,
					ex);
		}
	}

	/**
	 * Checks the given bean name whether it is unique. If not already unique, a
	 * counter is added, increasing the counter until the name is unique.
	 * 
	 * @param innerBeanName
	 *            the original name for the inner bean
	 * @return the adapted name for the inner bean
	 */
	private String adaptInnerBeanName(String innerBeanName) {
		String actualInnerBeanName = innerBeanName;
		int counter = 0;
		while (this.beanFactory.isBeanNameInUse(actualInnerBeanName)) {
			counter++;
			actualInnerBeanName = innerBeanName + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR + counter;
		}
		return actualInnerBeanName;
	}

	/**
	 * Resolve a reference to another bean in the factory.
	 */
	private Object resolveReference(Object argName, RuntimeBeanReference ref) {
		try {
			String refName = ref.getBeanName();
			refName = String.valueOf(doEvaluate(refName));
			if (ref.isToParent()) {
				if (this.beanFactory.getParentBeanFactory() == null) {
					throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName,
							"Can't resolve reference to bean '" + refName
									+ "' in parent factory: no parent factory available");
				}
				return this.beanFactory.getParentBeanFactory().getBean(refName);
			} else {
				Object bean = this.beanFactory.getBean(refName);
				this.beanFactory.registerDependentBean(refName, this.beanName);
				return bean;
			}
		} catch (BeansException ex) {
			throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName,
					"Cannot resolve reference to bean '" + ref.getBeanName() + "' while setting " + argName, ex);
		}
	}

	/**
	 * For each element in the managed array, resolve reference if necessary.
	 */
	private Object resolveManagedArray(Object argName, List<?> ml, Class<?> elementType) {
		Object resolved = Array.newInstance(elementType, ml.size());
		for (int i = 0; i < ml.size(); i++) {
			Array.set(resolved, i, resolveValueIfNecessary(new KeyedArgName(argName, i), ml.get(i)));
		}
		return resolved;
	}

	/**
	 * For each element in the managed list, resolve reference if necessary.
	 */
	private List<?> resolveManagedList(Object argName, List<?> ml) {
		List<Object> resolved = new ArrayList<Object>(ml.size());
		for (int i = 0; i < ml.size(); i++) {
			resolved.add(resolveValueIfNecessary(new KeyedArgName(argName, i), ml.get(i)));
		}
		return resolved;
	}

	/**
	 * For each element in the managed set, resolve reference if necessary.
	 */
	private Set<?> resolveManagedSet(Object argName, Set<?> ms) {
		Set<Object> resolved = new LinkedHashSet<Object>(ms.size());
		int i = 0;
		for (Object m : ms) {
			resolved.add(resolveValueIfNecessary(new KeyedArgName(argName, i), m));
			i++;
		}
		return resolved;
	}

	/**
	 * For each element in the managed map, resolve reference if necessary.
	 */
	private Map<?, ?> resolveManagedMap(Object argName, Map<?, ?> mm) {
		Map<Object, Object> resolved = new LinkedHashMap<Object, Object>(mm.size());
		for (Map.Entry<?, ?> entry : mm.entrySet()) {
			Object resolvedKey = resolveValueIfNecessary(argName, entry.getKey());
			Object resolvedValue = resolveValueIfNecessary(new KeyedArgName(argName, entry.getKey()), entry.getValue());
			resolved.put(resolvedKey, resolvedValue);
		}
		return resolved;
	}

	/**
	 * Holder class used for delayed toString building.
	 */
	private static class KeyedArgName {

		private final Object argName;

		private final Object key;

		public KeyedArgName(Object argName, Object key) {
			this.argName = argName;
			this.key = key;
		}

		@Override
		public String toString() {
			return this.argName + " with key " + BeanWrapper.PROPERTY_KEY_PREFIX + this.key
					+ BeanWrapper.PROPERTY_KEY_SUFFIX;
		}
	}

}
