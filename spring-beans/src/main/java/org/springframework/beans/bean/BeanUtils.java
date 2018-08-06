package org.springframework.beans.bean;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.GenericTypeAwarePropertyDescriptor;
import org.springframework.beans.exception.BeanInstantiationException;
import org.springframework.beans.exception.BeansException;
import org.springframework.beans.exception.FatalBeanException;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public abstract class BeanUtils {

    private static final Log logger = LogFactory.getLog(BeanUtils.class);

    private static final Set<Class<?>> unknownEditorTypes = Collections.newSetFromMap(new ConcurrentReferenceHashMap<Class<?>, Boolean>(64));

    //实例化
    public static <T> T instantiate(Class<T> clazz) throws org.springframework.beans.exception.BeanInstantiationException {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface()) {
            throw new org.springframework.beans.exception.BeanInstantiationException(clazz, "Specified class is an interface");
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException ex) {
            throw new org.springframework.beans.exception.BeanInstantiationException(clazz, "Is it an abstract class?", ex);
        } catch (IllegalAccessException ex) {
            throw new org.springframework.beans.exception.BeanInstantiationException(clazz, "Is the constructor accessible?", ex);
        }
    }

    //实例化类型
    public static <T> T instantiateClass(Class<T> clazz) throws org.springframework.beans.exception.BeanInstantiationException {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface()) {
            throw new org.springframework.beans.exception.BeanInstantiationException(clazz, "Specified class is an interface");
        }
        try {
            return instantiateClass(clazz.getDeclaredConstructor());
        } catch (NoSuchMethodException ex) {
            throw new org.springframework.beans.exception.BeanInstantiationException(clazz, "No default constructor found", ex);
        }
    }

    //实例化类型
    @SuppressWarnings("unchecked")
    public static <T> T instantiateClass(Class<?> clazz, Class<T> assignableTo) throws org.springframework.beans.exception.BeanInstantiationException {
        Assert.isAssignable(assignableTo, clazz);
        return (T) instantiateClass(clazz);
    }

    //实例化类型
    public static <T> T instantiateClass(Constructor<T> ctor, Object... args) throws org.springframework.beans.exception.BeanInstantiationException {
        Assert.notNull(ctor, "Constructor must not be null");
        try {
            ReflectionUtils.makeAccessible(ctor);
            return ctor.newInstance(args);
        } catch (InstantiationException ex) {
            throw new org.springframework.beans.exception.BeanInstantiationException(ctor, "Is it an abstract class?", ex);
        } catch (IllegalAccessException ex) {
            throw new org.springframework.beans.exception.BeanInstantiationException(ctor, "Is the constructor accessible?", ex);
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.beans.exception.BeanInstantiationException(ctor, "Illegal arguments for constructor", ex);
        } catch (InvocationTargetException ex) {
            throw new BeanInstantiationException(ctor, "Constructor threw exception", ex.getTargetException());
        }
    }

    //寻找方面
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            return findDeclaredMethod(clazz, methodName, paramTypes);
        }
    }

    //寻找声明方法
    public static Method findDeclaredMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            if (clazz.getSuperclass() != null) {
                return findDeclaredMethod(clazz.getSuperclass(), methodName, paramTypes);
            }
            return null;
        }
    }

    //寻找最小参数方法
    public static Method findMethodWithMinimalParameters(Class<?> clazz, String methodName)
            throws IllegalArgumentException {
        Method targetMethod = findMethodWithMinimalParameters(clazz.getMethods(), methodName);
        if (targetMethod == null) {
            targetMethod = findDeclaredMethodWithMinimalParameters(clazz, methodName);
        }
        return targetMethod;
    }

    //寻找最小参数的声明方法
    public static Method findDeclaredMethodWithMinimalParameters(Class<?> clazz, String methodName)
            throws IllegalArgumentException {
        Method targetMethod = findMethodWithMinimalParameters(clazz.getDeclaredMethods(), methodName);
        if (targetMethod == null && clazz.getSuperclass() != null) {
            targetMethod = findDeclaredMethodWithMinimalParameters(clazz.getSuperclass(), methodName);
        }
        return targetMethod;
    }

    //寻找最小参数的方法
    public static Method findMethodWithMinimalParameters(Method[] methods, String methodName)
            throws IllegalArgumentException {
        Method targetMethod = null;
        int numMethodsFoundWithCurrentMinimumArgs = 0;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                int numParams = method.getParameterTypes().length;
                if (targetMethod == null || numParams < targetMethod.getParameterTypes().length) {
                    targetMethod = method;
                    numMethodsFoundWithCurrentMinimumArgs = 1;
                } else if (!method.isBridge() && targetMethod.getParameterTypes().length == numParams) {
                    if (targetMethod.isBridge()) {
                        // Prefer regular method over bridge...
                        targetMethod = method;
                    } else {
                        // Additional candidate with same length
                        numMethodsFoundWithCurrentMinimumArgs++;
                    }
                }
            }
        }
        if (numMethodsFoundWithCurrentMinimumArgs > 1) {
            throw new IllegalArgumentException("Cannot resolve method '" + methodName
                    + "' to a unique method. Attempted to resolve to overloaded method with "
                    + "the least number of parameters but there were " + numMethodsFoundWithCurrentMinimumArgs
                    + " candidates.");
        }
        return targetMethod;
    }

    //解析方法签名
    public static Method resolveSignature(String signature, Class<?> clazz) {
        Assert.hasText(signature, "'signature' must not be empty");
        Assert.notNull(clazz, "Class must not be null");
        int firstParen = signature.indexOf("(");
        int lastParen = signature.indexOf(")");
        if (firstParen > -1 && lastParen == -1) {
            throw new IllegalArgumentException(
                    "Invalid method signature '" + signature + "': expected closing ')' for args list");
        } else if (lastParen > -1 && firstParen == -1) {
            throw new IllegalArgumentException(
                    "Invalid method signature '" + signature + "': expected opening '(' for args list");
        } else if (firstParen == -1 && lastParen == -1) {
            return findMethodWithMinimalParameters(clazz, signature);
        } else {
            String methodName = signature.substring(0, firstParen);
            String[] parameterTypeNames = StringUtils
                    .commaDelimitedListToStringArray(signature.substring(firstParen + 1, lastParen));
            Class<?>[] parameterTypes = new Class<?>[parameterTypeNames.length];
            for (int i = 0; i < parameterTypeNames.length; i++) {
                String parameterTypeName = parameterTypeNames[i].trim();
                try {
                    parameterTypes[i] = ClassUtils.forName(parameterTypeName, clazz.getClassLoader());
                } catch (Throwable ex) {
                    throw new IllegalArgumentException("Invalid method signature: unable to resolve type ["
                            + parameterTypeName + "] for argument " + i + ". Root cause: " + ex);
                }
            }
            return findMethod(clazz, methodName, parameterTypes);
        }
    }

    //获取属性描述符
    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws org.springframework.beans.exception.BeansException {
        CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
        return cr.getPropertyDescriptors();
    }

    //获取属性描述符
    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) throws org.springframework.beans.exception.BeansException {
        CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
        return cr.getPropertyDescriptor(propertyName);
    }


    public static PropertyDescriptor findPropertyForMethod(Method method) throws org.springframework.beans.exception.BeansException {
        return findPropertyForMethod(method, method.getDeclaringClass());
    }

    /**
     * Find a JavaBeans {@code PropertyDescriptor} for the given method, with the
     * method either being the read method or the write method for that bean
     * property.
     *
     * @param method the method to find a corresponding PropertyDescriptor for
     * @param clazz  the (most specific) class to introspect for descriptors
     * @return the corresponding PropertyDescriptor, or {@code null} if none
     * @throws org.springframework.beans.exception.BeansException if PropertyDescriptor lookup fails
     * @since 3.2.13
     */
    public static PropertyDescriptor findPropertyForMethod(Method method, Class<?> clazz) throws org.springframework.beans.exception.BeansException {
        Assert.notNull(method, "Method must not be null");
        PropertyDescriptor[] pds = getPropertyDescriptors(clazz);
        for (PropertyDescriptor pd : pds) {
            if (method.equals(pd.getReadMethod()) || method.equals(pd.getWriteMethod())) {
                return pd;
            }
        }
        return null;
    }

    //获取属性编辑器
    public static PropertyEditor findEditorByConvention(Class<?> targetType) {
        if (targetType == null || targetType.isArray() || unknownEditorTypes.contains(targetType)) {
            return null;
        }
        ClassLoader cl = targetType.getClassLoader();
        if (cl == null) {
            try {
                cl = ClassLoader.getSystemClassLoader();
                if (cl == null) {
                    return null;
                }
            } catch (Throwable ex) {
                // e.g. AccessControlException on Google App Engine
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not access system ClassLoader: " + ex);
                }
                return null;
            }
        }
        String editorName = targetType.getName() + "Editor";
        try {
            Class<?> editorClass = cl.loadClass(editorName);
            if (!PropertyEditor.class.isAssignableFrom(editorClass)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Editor class [" + editorName
                            + "] does not implement [java.beans.PropertyEditor] interface");
                }
                unknownEditorTypes.add(targetType);
                return null;
            }
            return (PropertyEditor) instantiateClass(editorClass);
        } catch (ClassNotFoundException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("No property editor [" + editorName + "] found for type " + targetType.getName()
                        + " according to 'Editor' suffix convention");
            }
            unknownEditorTypes.add(targetType);
            return null;
        }
    }

    //寻找属性类型
    public static Class<?> findPropertyType(String propertyName, Class<?>... beanClasses) {
        if (beanClasses != null) {
            for (Class<?> beanClass : beanClasses) {
                PropertyDescriptor pd = getPropertyDescriptor(beanClass, propertyName);
                if (pd != null) {
                    return pd.getPropertyType();
                }
            }
        }
        return Object.class;
    }

    //获取写入方法参数
    public static MethodParameter getWriteMethodParameter(PropertyDescriptor pd) {
        if (pd instanceof GenericTypeAwarePropertyDescriptor) {
            return new MethodParameter(((GenericTypeAwarePropertyDescriptor) pd).getWriteMethodParameter());
        } else {
            return new MethodParameter(pd.getWriteMethod(), 0);
        }
    }

    //是否是简单属性
    public static boolean isSimpleProperty(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return isSimpleValueType(clazz) || (clazz.isArray() && isSimpleValueType(clazz.getComponentType()));
    }

    //是否是简单值类型
    public static boolean isSimpleValueType(Class<?> clazz) {
        return (ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isEnum() || CharSequence.class.isAssignableFrom(clazz)
                || Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz) || URI.class == clazz
                || URL.class == clazz || Locale.class == clazz || Class.class == clazz);
    }

    //复制属性
    public static void copyProperties(Object source, Object target) throws org.springframework.beans.exception.BeansException {
        copyProperties(source, target, null, (String[]) null);
    }

    //复制属性
    public static void copyProperties(Object source, Object target, Class<?> editable) throws org.springframework.beans.exception.BeansException {
        copyProperties(source, target, editable, (String[]) null);
    }

    //复制属性
    public static void copyProperties(Object source, Object target, String... ignoreProperties) throws org.springframework.beans.exception.BeansException {
        copyProperties(source, target, null, ignoreProperties);
    }

    //复制属性
    private static void copyProperties(Object source, Object target, Class<?> editable, String... ignoreProperties)
            throws BeansException {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");
        Class<?> actualEditable = target.getClass();
        if (editable != null) {
            if (!editable.isInstance(target)) {
                throw new IllegalArgumentException("Target class [" + target.getClass().getName()
                        + "] not assignable to Editable class [" + editable.getName() + "]");
            }
            actualEditable = editable;
        }
        PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
        List<String> ignoreList = (ignoreProperties != null ? Arrays.asList(ignoreProperties) : null);
        for (PropertyDescriptor targetPd : targetPds) {
            Method writeMethod = targetPd.getWriteMethod();
            if (writeMethod != null && (ignoreList == null || !ignoreList.contains(targetPd.getName()))) {
                PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
                if (sourcePd != null) {
                    Method readMethod = sourcePd.getReadMethod();
                    if (readMethod != null && ClassUtils.isAssignable(writeMethod.getParameterTypes()[0],
                            readMethod.getReturnType())) {
                        try {
                            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                readMethod.setAccessible(true);
                            }
                            Object value = readMethod.invoke(source);
                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                writeMethod.setAccessible(true);
                            }
                            writeMethod.invoke(target, value);
                        } catch (Throwable ex) {
                            throw new FatalBeanException(
                                    "Could not copy property '" + targetPd.getName() + "' from source to target", ex);
                        }
                    }
                }
            }
        }
    }

}
