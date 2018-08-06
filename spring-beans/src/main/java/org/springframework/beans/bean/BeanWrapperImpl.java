package org.springframework.beans.bean;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.GenericTypeAwarePropertyDescriptor;
import org.springframework.beans.exception.InvalidPropertyException;
import org.springframework.beans.exception.NotWritablePropertyException;
import org.springframework.beans.exception.TypeMismatchException;
import org.springframework.beans.property.accessor.AbstractNestablePropertyAccessor;
import org.springframework.beans.property.PropertyMatches;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.Property;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;

//Bean包装器实现类
public class BeanWrapperImpl extends AbstractNestablePropertyAccessor implements BeanWrapper {

	private CachedIntrospectionResults cachedIntrospectionResults;
	private AccessControlContext acc;                               //访问控制上下文

	public BeanWrapperImpl() {
		this(true);
	}

	public BeanWrapperImpl(boolean registerDefaultEditors) {
		super(registerDefaultEditors);
	}

	public BeanWrapperImpl(Object object) {
		super(object);
	}

	public BeanWrapperImpl(Class<?> clazz) {
		super(clazz);
	}

	public BeanWrapperImpl(Object object, String nestedPath, Object rootObject) {
		super(object, nestedPath, rootObject);
	}

	private BeanWrapperImpl(Object object, String nestedPath, BeanWrapperImpl parent) {
		super(object, nestedPath, parent);
		setSecurityContext(parent.acc);
	}

	//设置Bean实例
	public void setBeanInstance(Object object) {
		this.wrappedObject = object;
		this.rootObject = object;
		this.typeConverterDelegate = new org.springframework.beans.property.type.TypeConverterDelegate(this, this.wrappedObject);
		setIntrospectionClass(object.getClass());
	}

	//设置包装类实例
	@Override
	public void setWrappedInstance(Object object, String nestedPath, Object rootObject) {
		super.setWrappedInstance(object, nestedPath, rootObject);
		setIntrospectionClass(getWrappedClass());
	}

	/**
	 * Set the class to introspect. Needs to be called when the target object
	 * changes.
	 * 
	 * @param clazz
	 *            the class to introspect
	 */
	protected void setIntrospectionClass(Class<?> clazz) {
		if (this.cachedIntrospectionResults != null && this.cachedIntrospectionResults.getBeanClass() != clazz) {
			this.cachedIntrospectionResults = null;
		}
	}

	/**
	 * Obtain a lazily initializted CachedIntrospectionResults instance for the
	 * wrapped object.
	 */
	private CachedIntrospectionResults getCachedIntrospectionResults() {
		Assert.state(getWrappedInstance() != null, "BeanWrapper does not hold a bean instance");
		if (this.cachedIntrospectionResults == null) {
			this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(getWrappedClass());
		}
		return this.cachedIntrospectionResults;
	}

	//设置访问控制上下文
	public void setSecurityContext(AccessControlContext acc) {
		this.acc = acc;
	}

	//获取访问控制上下文
	public AccessControlContext getSecurityContext() {
		return this.acc;
	}

	//转换属性
	public Object convertForProperty(Object value, String propertyName) throws TypeMismatchException {
		CachedIntrospectionResults cachedIntrospectionResults = getCachedIntrospectionResults();
		PropertyDescriptor pd = cachedIntrospectionResults.getPropertyDescriptor(propertyName);
		if (pd == null) {
			throw new org.springframework.beans.exception.InvalidPropertyException(getRootClass(), getNestedPath() + propertyName,
					"No property '" + propertyName + "' found");
		}
		TypeDescriptor td = cachedIntrospectionResults.getTypeDescriptor(pd);
		if (td == null) {
			td = cachedIntrospectionResults.addTypeDescriptor(pd, new TypeDescriptor(property(pd)));
		}
		return convertForProperty(propertyName, null, value, td);
	}

	//根据描述符获取属性
	private Property property(PropertyDescriptor pd) {
		GenericTypeAwarePropertyDescriptor gpd = (GenericTypeAwarePropertyDescriptor) pd;
		return new Property(gpd.getBeanClass(), gpd.getReadMethod(), gpd.getWriteMethod(), gpd.getName());
	}

	//获取本地属性处理器
	@Override
	protected BeanPropertyHandler getLocalPropertyHandler(String propertyName) {
		PropertyDescriptor pd = getCachedIntrospectionResults().getPropertyDescriptor(propertyName);
		if (pd != null) {
			return new BeanPropertyHandler(pd);
		}
		return null;
	}

	//新建嵌套属性访问器
	@Override
	protected BeanWrapperImpl newNestedPropertyAccessor(Object object, String nestedPath) {
		return new BeanWrapperImpl(object, nestedPath, this);
	}

	//创建属性不可写异常
	@Override
	protected org.springframework.beans.exception.NotWritablePropertyException createNotWritablePropertyException(String propertyName) {
		org.springframework.beans.property.PropertyMatches matches = PropertyMatches.forProperty(propertyName, getRootClass());
		throw new NotWritablePropertyException(getRootClass(), getNestedPath() + propertyName,
				matches.buildErrorMessage(), matches.getPossibleMatches());
	}

	//获取所有属性描述符
	@Override
	public PropertyDescriptor[] getPropertyDescriptors() {
		return getCachedIntrospectionResults().getPropertyDescriptors();
	}

	//获取属性描述符
	@Override
	public PropertyDescriptor getPropertyDescriptor(String propertyName) throws org.springframework.beans.exception.InvalidPropertyException {
		BeanWrapperImpl nestedBw = (BeanWrapperImpl) getPropertyAccessorForPropertyPath(propertyName);
		String finalPath = getFinalPath(nestedBw, propertyName);
		PropertyDescriptor pd = nestedBw.getCachedIntrospectionResults().getPropertyDescriptor(finalPath);
		if (pd == null) {
			throw new InvalidPropertyException(getRootClass(), getNestedPath() + propertyName,
					"No property '" + propertyName + "' found");
		}
		return pd;
	}


	//Bean属性处理器
	private class BeanPropertyHandler extends PropertyHandler {

		private final PropertyDescriptor pd;

		public BeanPropertyHandler(PropertyDescriptor pd) {
			super(pd.getPropertyType(), pd.getReadMethod() != null, pd.getWriteMethod() != null);
			this.pd = pd;
		}

		@Override
		public ResolvableType getResolvableType() {
			return ResolvableType.forMethodReturnType(this.pd.getReadMethod());
		}

		@Override
		public TypeDescriptor toTypeDescriptor() {
			return new TypeDescriptor(property(this.pd));
		}

		@Override
		public TypeDescriptor nested(int level) {
			return TypeDescriptor.nested(property(pd), level);
		}

		@Override
		public Object getValue() throws Exception {
			final Method readMethod = this.pd.getReadMethod();
			if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers()) && !readMethod.isAccessible()) {
				if (System.getSecurityManager() != null) {
					AccessController.doPrivileged(new PrivilegedAction<Object>() {
						@Override
						public Object run() {
							readMethod.setAccessible(true);
							return null;
						}
					});
				} else {
					readMethod.setAccessible(true);
				}
			}
			if (System.getSecurityManager() != null) {
				try {
					return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
						@Override
						public Object run() throws Exception {
							return readMethod.invoke(getWrappedInstance(), (Object[]) null);
						}
					}, acc);
				} catch (PrivilegedActionException pae) {
					throw pae.getException();
				}
			} else {
				return readMethod.invoke(getWrappedInstance(), (Object[]) null);
			}
		}

		@Override
		public void setValue(final Object object, Object valueToApply) throws Exception {
			final Method writeMethod = (this.pd instanceof GenericTypeAwarePropertyDescriptor
					? ((GenericTypeAwarePropertyDescriptor) this.pd).getWriteMethodForActualAccess()
					: this.pd.getWriteMethod());
			if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers()) && !writeMethod.isAccessible()) {
				if (System.getSecurityManager() != null) {
					AccessController.doPrivileged(new PrivilegedAction<Object>() {
						@Override
						public Object run() {
							writeMethod.setAccessible(true);
							return null;
						}
					});
				} else {
					writeMethod.setAccessible(true);
				}
			}
			final Object value = valueToApply;
			if (System.getSecurityManager() != null) {
				try {
					AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
						@Override
						public Object run() throws Exception {
							writeMethod.invoke(object, value);
							return null;
						}
					}, acc);
				} catch (PrivilegedActionException ex) {
					throw ex.getException();
				}
			} else {
				writeMethod.invoke(getWrappedInstance(), value);
			}
		}
	}

}
