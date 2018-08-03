package org.springframework.beans;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.ObjectUtils;

//扩展的Bean信息
class ExtendedBeanInfo implements BeanInfo {

	private static final Log logger = LogFactory.getLog(ExtendedBeanInfo.class);

	private final BeanInfo delegate;

	private final Set<PropertyDescriptor> propertyDescriptors = new TreeSet<PropertyDescriptor>(new PropertyDescriptorComparator());

	public ExtendedBeanInfo(BeanInfo delegate) throws IntrospectionException {
		this.delegate = delegate;
		for (PropertyDescriptor pd : delegate.getPropertyDescriptors()) {
			try {
				this.propertyDescriptors.add(pd instanceof IndexedPropertyDescriptor
						? new SimpleIndexedPropertyDescriptor((IndexedPropertyDescriptor) pd)
						: new SimplePropertyDescriptor(pd));
			} catch (IntrospectionException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring invalid bean property '" + pd.getName() + "': " + ex.getMessage());
				}
			}
		}
		MethodDescriptor[] methodDescriptors = delegate.getMethodDescriptors();
		if (methodDescriptors != null) {
			for (Method method : findCandidateWriteMethods(methodDescriptors)) {
				try {
					handleCandidateWriteMethod(method);
				} catch (IntrospectionException ex) {
					// We're only trying to find candidates, can easily ignore extra ones here...
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring candidate write method [" + method + "]: " + ex.getMessage());
					}
				}
			}
		}
	}

	private List<Method> findCandidateWriteMethods(MethodDescriptor[] methodDescriptors) {
		List<Method> matches = new ArrayList<Method>();
		for (MethodDescriptor methodDescriptor : methodDescriptors) {
			Method method = methodDescriptor.getMethod();
			if (isCandidateWriteMethod(method)) {
				matches.add(method);
			}
		}
		Collections.sort(matches, new Comparator<Method>() {
			@Override
			public int compare(Method m1, Method m2) {
				return m2.toString().compareTo(m1.toString());
			}
		});
		return matches;
	}

	public static boolean isCandidateWriteMethod(Method method) {
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		int nParams = parameterTypes.length;
		return (methodName.length() > 3 && methodName.startsWith("set") && Modifier.isPublic(method.getModifiers())
				&& (!void.class.isAssignableFrom(method.getReturnType()) || Modifier.isStatic(method.getModifiers()))
				&& (nParams == 1 || (nParams == 2 && int.class == parameterTypes[0])));
	}

	private void handleCandidateWriteMethod(Method method) throws IntrospectionException {
		int nParams = method.getParameterTypes().length;
		String propertyName = propertyNameFor(method);
		Class<?> propertyType = method.getParameterTypes()[nParams - 1];
		PropertyDescriptor existingPd = findExistingPropertyDescriptor(propertyName, propertyType);
		if (nParams == 1) {
			if (existingPd == null) {
				this.propertyDescriptors.add(new SimplePropertyDescriptor(propertyName, null, method));
			} else {
				existingPd.setWriteMethod(method);
			}
		} else if (nParams == 2) {
			if (existingPd == null) {
				this.propertyDescriptors
						.add(new SimpleIndexedPropertyDescriptor(propertyName, null, null, null, method));
			} else if (existingPd instanceof IndexedPropertyDescriptor) {
				((IndexedPropertyDescriptor) existingPd).setIndexedWriteMethod(method);
			} else {
				this.propertyDescriptors.remove(existingPd);
				this.propertyDescriptors.add(new SimpleIndexedPropertyDescriptor(propertyName,
						existingPd.getReadMethod(), existingPd.getWriteMethod(), null, method));
			}
		} else {
			throw new IllegalArgumentException("Write method must have exactly 1 or 2 parameters: " + method);
		}
	}

	private PropertyDescriptor findExistingPropertyDescriptor(String propertyName, Class<?> propertyType) {
		for (PropertyDescriptor pd : this.propertyDescriptors) {
			final Class<?> candidateType;
			final String candidateName = pd.getName();
			if (pd instanceof IndexedPropertyDescriptor) {
				IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
				candidateType = ipd.getIndexedPropertyType();
				if (candidateName.equals(propertyName) && (candidateType.equals(propertyType)
						|| candidateType.equals(propertyType.getComponentType()))) {
					return pd;
				}
			} else {
				candidateType = pd.getPropertyType();
				if (candidateName.equals(propertyName) && (candidateType.equals(propertyType)
						|| propertyType.equals(candidateType.getComponentType()))) {
					return pd;
				}
			}
		}
		return null;
	}

	private String propertyNameFor(Method method) {
		return Introspector.decapitalize(method.getName().substring(3, method.getName().length()));
	}

	/**
	 * Return the set of {@link PropertyDescriptor}s from the wrapped
	 * {@link BeanInfo} object as well as {@code PropertyDescriptor}s for each
	 * non-void returning setter method found during construction.
	 * 
	 * @see #ExtendedBeanInfo(BeanInfo)
	 */
	@Override
	public PropertyDescriptor[] getPropertyDescriptors() {
		return this.propertyDescriptors.toArray(new PropertyDescriptor[this.propertyDescriptors.size()]);
	}

	@Override
	public BeanInfo[] getAdditionalBeanInfo() {
		return this.delegate.getAdditionalBeanInfo();
	}

	@Override
	public BeanDescriptor getBeanDescriptor() {
		return this.delegate.getBeanDescriptor();
	}

	@Override
	public int getDefaultEventIndex() {
		return this.delegate.getDefaultEventIndex();
	}

	@Override
	public int getDefaultPropertyIndex() {
		return this.delegate.getDefaultPropertyIndex();
	}

	@Override
	public EventSetDescriptor[] getEventSetDescriptors() {
		return this.delegate.getEventSetDescriptors();
	}

	@Override
	public Image getIcon(int iconKind) {
		return this.delegate.getIcon(iconKind);
	}

	@Override
	public MethodDescriptor[] getMethodDescriptors() {
		return this.delegate.getMethodDescriptors();
	}

	static class SimplePropertyDescriptor extends PropertyDescriptor {

		private Method readMethod;

		private Method writeMethod;

		private Class<?> propertyType;

		private Class<?> propertyEditorClass;

		public SimplePropertyDescriptor(PropertyDescriptor original) throws IntrospectionException {
			this(original.getName(), original.getReadMethod(), original.getWriteMethod());
			PropertyDescriptorUtils.copyNonMethodProperties(original, this);
		}

		public SimplePropertyDescriptor(String propertyName, Method readMethod, Method writeMethod)
				throws IntrospectionException {
			super(propertyName, null, null);
			this.readMethod = readMethod;
			this.writeMethod = writeMethod;
			this.propertyType = PropertyDescriptorUtils.findPropertyType(readMethod, writeMethod);
		}

		@Override
		public Method getReadMethod() {
			return this.readMethod;
		}

		@Override
		public void setReadMethod(Method readMethod) {
			this.readMethod = readMethod;
		}

		@Override
		public Method getWriteMethod() {
			return this.writeMethod;
		}

		@Override
		public void setWriteMethod(Method writeMethod) {
			this.writeMethod = writeMethod;
		}

		@Override
		public Class<?> getPropertyType() {
			if (this.propertyType == null) {
				try {
					this.propertyType = PropertyDescriptorUtils.findPropertyType(this.readMethod, this.writeMethod);
				} catch (IntrospectionException ex) {
					// Ignore, as does PropertyDescriptor#getPropertyType
				}
			}
			return this.propertyType;
		}

		@Override
		public Class<?> getPropertyEditorClass() {
			return this.propertyEditorClass;
		}

		@Override
		public void setPropertyEditorClass(Class<?> propertyEditorClass) {
			this.propertyEditorClass = propertyEditorClass;
		}

		@Override
		public boolean equals(Object other) {
			return (this == other || (other instanceof PropertyDescriptor
					&& PropertyDescriptorUtils.equals(this, (PropertyDescriptor) other)));
		}

		@Override
		public int hashCode() {
			return (ObjectUtils.nullSafeHashCode(getReadMethod()) * 29
					+ ObjectUtils.nullSafeHashCode(getWriteMethod()));
		}

		@Override
		public String toString() {
			return String.format("%s[name=%s, propertyType=%s, readMethod=%s, writeMethod=%s]",
					getClass().getSimpleName(), getName(), getPropertyType(), this.readMethod, this.writeMethod);
		}
	}

	static class SimpleIndexedPropertyDescriptor extends IndexedPropertyDescriptor {

		private Method readMethod;

		private Method writeMethod;

		private Class<?> propertyType;

		private Method indexedReadMethod;

		private Method indexedWriteMethod;

		private Class<?> indexedPropertyType;

		private Class<?> propertyEditorClass;

		public SimpleIndexedPropertyDescriptor(IndexedPropertyDescriptor original) throws IntrospectionException {
			this(original.getName(), original.getReadMethod(), original.getWriteMethod(),
					original.getIndexedReadMethod(), original.getIndexedWriteMethod());
			PropertyDescriptorUtils.copyNonMethodProperties(original, this);
		}

		public SimpleIndexedPropertyDescriptor(String propertyName, Method readMethod, Method writeMethod,
				Method indexedReadMethod, Method indexedWriteMethod) throws IntrospectionException {
			super(propertyName, null, null, null, null);
			this.readMethod = readMethod;
			this.writeMethod = writeMethod;
			this.propertyType = PropertyDescriptorUtils.findPropertyType(readMethod, writeMethod);
			this.indexedReadMethod = indexedReadMethod;
			this.indexedWriteMethod = indexedWriteMethod;
			this.indexedPropertyType = PropertyDescriptorUtils.findIndexedPropertyType(propertyName, this.propertyType,
					indexedReadMethod, indexedWriteMethod);
		}

		@Override
		public Method getReadMethod() {
			return this.readMethod;
		}

		@Override
		public void setReadMethod(Method readMethod) {
			this.readMethod = readMethod;
		}

		@Override
		public Method getWriteMethod() {
			return this.writeMethod;
		}

		@Override
		public void setWriteMethod(Method writeMethod) {
			this.writeMethod = writeMethod;
		}

		@Override
		public Class<?> getPropertyType() {
			if (this.propertyType == null) {
				try {
					this.propertyType = PropertyDescriptorUtils.findPropertyType(this.readMethod, this.writeMethod);
				} catch (IntrospectionException ex) {
					// Ignore, as does IndexedPropertyDescriptor#getPropertyType
				}
			}
			return this.propertyType;
		}

		@Override
		public Method getIndexedReadMethod() {
			return this.indexedReadMethod;
		}

		@Override
		public void setIndexedReadMethod(Method indexedReadMethod) throws IntrospectionException {
			this.indexedReadMethod = indexedReadMethod;
		}

		@Override
		public Method getIndexedWriteMethod() {
			return this.indexedWriteMethod;
		}

		@Override
		public void setIndexedWriteMethod(Method indexedWriteMethod) throws IntrospectionException {
			this.indexedWriteMethod = indexedWriteMethod;
		}

		@Override
		public Class<?> getIndexedPropertyType() {
			if (this.indexedPropertyType == null) {
				try {
					this.indexedPropertyType = PropertyDescriptorUtils.findIndexedPropertyType(getName(),
							getPropertyType(), this.indexedReadMethod, this.indexedWriteMethod);
				} catch (IntrospectionException ex) {
					// Ignore, as does IndexedPropertyDescriptor#getIndexedPropertyType
				}
			}
			return this.indexedPropertyType;
		}

		@Override
		public Class<?> getPropertyEditorClass() {
			return this.propertyEditorClass;
		}

		@Override
		public void setPropertyEditorClass(Class<?> propertyEditorClass) {
			this.propertyEditorClass = propertyEditorClass;
		}

		/*
		 * See java.beans.IndexedPropertyDescriptor#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof IndexedPropertyDescriptor)) {
				return false;
			}
			IndexedPropertyDescriptor otherPd = (IndexedPropertyDescriptor) other;
			return (ObjectUtils.nullSafeEquals(getIndexedReadMethod(), otherPd.getIndexedReadMethod())
					&& ObjectUtils.nullSafeEquals(getIndexedWriteMethod(), otherPd.getIndexedWriteMethod())
					&& ObjectUtils.nullSafeEquals(getIndexedPropertyType(), otherPd.getIndexedPropertyType())
					&& PropertyDescriptorUtils.equals(this, otherPd));
		}

		@Override
		public int hashCode() {
			int hashCode = ObjectUtils.nullSafeHashCode(getReadMethod());
			hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(getWriteMethod());
			hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(getIndexedReadMethod());
			hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(getIndexedWriteMethod());
			return hashCode;
		}

		@Override
		public String toString() {
			return String.format(
					"%s[name=%s, propertyType=%s, indexedPropertyType=%s, "
							+ "readMethod=%s, writeMethod=%s, indexedReadMethod=%s, indexedWriteMethod=%s]",
					getClass().getSimpleName(), getName(), getPropertyType(), getIndexedPropertyType(), this.readMethod,
					this.writeMethod, this.indexedReadMethod, this.indexedWriteMethod);
		}
	}

	/**
	 * Sorts PropertyDescriptor instances alpha-numerically to emulate the behavior
	 * of {@link java.beans.BeanInfo#getPropertyDescriptors()}.
	 * 
	 * @see ExtendedBeanInfo#propertyDescriptors
	 */
	static class PropertyDescriptorComparator implements Comparator<PropertyDescriptor> {
		@Override
		public int compare(PropertyDescriptor desc1, PropertyDescriptor desc2) {
			String left = desc1.getName();
			String right = desc2.getName();
			for (int i = 0; i < left.length(); i++) {
				if (right.length() == i) {
					return 1;
				}
				int result = left.getBytes()[i] - right.getBytes()[i];
				if (result != 0) {
					return result;
				}
			}
			return left.length() - right.length();
		}
	}

}
