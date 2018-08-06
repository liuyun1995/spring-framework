package org.springframework.beans.support.autowire;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import org.springframework.beans.bean.BeanMetadataElement;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

//自动装配工具
abstract class AutowireUtils {

    //排序构造器
    public static void sortConstructors(Constructor<?>[] constructors) {
        Arrays.sort(constructors, new Comparator<Constructor<?>>() {
            @Override
            public int compare(Constructor<?> c1, Constructor<?> c2) {
                boolean p1 = Modifier.isPublic(c1.getModifiers());
                boolean p2 = Modifier.isPublic(c2.getModifiers());
                if (p1 != p2) {
                    return (p1 ? -1 : 1);
                }
                int c1pl = c1.getParameterTypes().length;
                int c2pl = c2.getParameterTypes().length;
                return (c1pl < c2pl ? 1 : (c1pl > c2pl ? -1 : 0));
            }
        });
    }

    //排序工厂方法
    public static void sortFactoryMethods(Method[] factoryMethods) {
        Arrays.sort(factoryMethods, new Comparator<Method>() {
            @Override
            public int compare(Method fm1, Method fm2) {
                boolean p1 = Modifier.isPublic(fm1.getModifiers());
                boolean p2 = Modifier.isPublic(fm2.getModifiers());
                if (p1 != p2) {
                    return (p1 ? -1 : 1);
                }
                int c1pl = fm1.getParameterTypes().length;
                int c2pl = fm2.getParameterTypes().length;
                return (c1pl < c2pl ? 1 : (c1pl > c2pl ? -1 : 0));
            }
        });
    }

    /**
     * Determine whether the given bean property is excluded from dependency checks.
     * <p>This implementation excludes properties defined by CGLIB.
     *
     * @param pd the PropertyDescriptor of the bean property
     * @return whether the bean property is excluded
     */
    public static boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
        Method wm = pd.getWriteMethod();
        if (wm == null) {
            return false;
        }
        if (!wm.getDeclaringClass().getName().contains("$$")) {
            // Not a CGLIB method so it's OK.
            return false;
        }
        // It was declared by CGLIB, but we might still want to autowire it
        // if it was actually declared by the superclass.
        Class<?> superclass = wm.getDeclaringClass().getSuperclass();
        return !ClassUtils.hasMethod(superclass, wm.getName(), wm.getParameterTypes());
    }

    /**
     * Return whether the setter method of the given bean property is defined
     * in any of the given interfaces.
     *
     * @param pd         the PropertyDescriptor of the bean property
     * @param interfaces the Set of interfaces (Class objects)
     * @return whether the setter method is defined by an interface
     */
    public static boolean isSetterDefinedInInterface(PropertyDescriptor pd, Set<Class<?>> interfaces) {
        Method setter = pd.getWriteMethod();
        if (setter != null) {
            Class<?> targetClass = setter.getDeclaringClass();
            for (Class<?> ifc : interfaces) {
                if (ifc.isAssignableFrom(targetClass) &&
                        ClassUtils.hasMethod(ifc, setter.getName(), setter.getParameterTypes())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Resolve the given autowiring value against the given required type,
     * e.g. an {@link ObjectFactory} value to its actual object result.
     *
     * @param autowiringValue the value to resolve
     * @param requiredType    the type to assign the result to
     * @return the resolved value
     */
    public static Object resolveAutowiringValue(Object autowiringValue, Class<?> requiredType) {
        if (autowiringValue instanceof ObjectFactory && !requiredType.isInstance(autowiringValue)) {
            ObjectFactory<?> factory = (ObjectFactory<?>) autowiringValue;
            if (autowiringValue instanceof Serializable && requiredType.isInterface()) {
                autowiringValue = Proxy.newProxyInstance(requiredType.getClassLoader(),
                        new Class<?>[]{requiredType}, new ObjectFactoryDelegatingInvocationHandler(factory));
            } else {
                return factory.getObject();
            }
        }
        return autowiringValue;
    }

    //从工厂方法中解析返回类型
    public static Class<?> resolveReturnTypeForFactoryMethod(Method method, Object[] args, ClassLoader classLoader) {
        Assert.notNull(method, "Method must not be null");
        Assert.notNull(args, "Argument array must not be null");
        Assert.notNull(classLoader, "ClassLoader must not be null");

        TypeVariable<Method>[] declaredTypeVariables = method.getTypeParameters();
        Type genericReturnType = method.getGenericReturnType();
        Type[] methodParameterTypes = method.getGenericParameterTypes();
        Assert.isTrue(args.length == methodParameterTypes.length, "Argument array does not match parameter count");

        // Ensure that the type variable (e.g., T) is declared directly on the method
        // itself (e.g., via <T>), not on the enclosing class or interface.
        boolean locallyDeclaredTypeVariableMatchesReturnType = false;
        for (TypeVariable<Method> currentTypeVariable : declaredTypeVariables) {
            if (currentTypeVariable.equals(genericReturnType)) {
                locallyDeclaredTypeVariableMatchesReturnType = true;
                break;
            }
        }

        if (locallyDeclaredTypeVariableMatchesReturnType) {
            for (int i = 0; i < methodParameterTypes.length; i++) {
                Type methodParameterType = methodParameterTypes[i];
                Object arg = args[i];
                if (methodParameterType.equals(genericReturnType)) {
                    if (arg instanceof TypedStringValue) {
                        TypedStringValue typedValue = ((TypedStringValue) arg);
                        if (typedValue.hasTargetType()) {
                            return typedValue.getTargetType();
                        }
                        try {
                            return typedValue.resolveTargetType(classLoader);
                        } catch (ClassNotFoundException ex) {
                            throw new IllegalStateException("Failed to resolve value type [" +
                                    typedValue.getTargetTypeName() + "] for factory method argument", ex);
                        }
                    }
                    // Only consider argument type if it is a simple value...
                    if (arg != null && !(arg instanceof BeanMetadataElement)) {
                        return arg.getClass();
                    }
                    return method.getReturnType();
                } else if (methodParameterType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) methodParameterType;
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    for (Type typeArg : actualTypeArguments) {
                        if (typeArg.equals(genericReturnType)) {
                            if (arg instanceof Class) {
                                return (Class<?>) arg;
                            } else {
                                String className = null;
                                if (arg instanceof String) {
                                    className = (String) arg;
                                } else if (arg instanceof TypedStringValue) {
                                    TypedStringValue typedValue = ((TypedStringValue) arg);
                                    String targetTypeName = typedValue.getTargetTypeName();
                                    if (targetTypeName == null || Class.class.getName().equals(targetTypeName)) {
                                        className = typedValue.getValue();
                                    }
                                }
                                if (className != null) {
                                    try {
                                        return ClassUtils.forName(className, classLoader);
                                    } catch (ClassNotFoundException ex) {
                                        throw new IllegalStateException("Could not resolve class name [" + arg +
                                                "] for factory method argument", ex);
                                    }
                                }
                                // Consider adding logic to determine the class of the typeArg, if possible.
                                // For now, just fall back...
                                return method.getReturnType();
                            }
                        }
                    }
                }
            }
        }

        // Fall back...
        return method.getReturnType();
    }


    /**
     * Reflective InvocationHandler for lazy access to the current target object.
     */
    @SuppressWarnings("serial")
    private static class ObjectFactoryDelegatingInvocationHandler implements InvocationHandler, Serializable {

        private final ObjectFactory<?> objectFactory;

        public ObjectFactoryDelegatingInvocationHandler(ObjectFactory<?> objectFactory) {
            this.objectFactory = objectFactory;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if (methodName.equals("equals")) {
                // Only consider equal when proxies are identical.
                return (proxy == args[0]);
            } else if (methodName.equals("hashCode")) {
                // Use hashCode of proxy.
                return System.identityHashCode(proxy);
            } else if (methodName.equals("toString")) {
                return this.objectFactory.toString();
            }
            try {
                return method.invoke(this.objectFactory.getObject(), args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }

}
