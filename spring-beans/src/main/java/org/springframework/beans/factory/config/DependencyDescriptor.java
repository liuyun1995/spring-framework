package org.springframework.beans.factory.config;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.InjectionPoint;
import org.springframework.beans.exception.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.support.autowire.AutowireCandidateResolver;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

//依赖描述符
@SuppressWarnings("serial")
public class DependencyDescriptor extends InjectionPoint implements Serializable {

    private final Class<?> declaringClass;
    private String methodName;
    private Class<?>[] parameterTypes;
    private int parameterIndex;
    private String fieldName;
    private final boolean required;
    private final boolean eager;
    private int nestingLevel = 1;
    private Class<?> containingClass;
    private volatile ResolvableType resolvableType;

    //构造器1
    public DependencyDescriptor(MethodParameter methodParameter, boolean required) {
        this(methodParameter, required, true);
    }

    //构造器2
    public DependencyDescriptor(MethodParameter methodParameter, boolean required, boolean eager) {
        super(methodParameter);
        this.declaringClass = methodParameter.getDeclaringClass();
        if (this.methodParameter.getMethod() != null) {
            this.methodName = methodParameter.getMethod().getName();
            this.parameterTypes = methodParameter.getMethod().getParameterTypes();
        } else {
            this.parameterTypes = methodParameter.getConstructor().getParameterTypes();
        }
        this.parameterIndex = methodParameter.getParameterIndex();
        this.containingClass = methodParameter.getContainingClass();
        this.required = required;
        this.eager = eager;
    }

    //构造器3
    public DependencyDescriptor(Field field, boolean required) {
        this(field, required, true);
    }

    //构造器4
    public DependencyDescriptor(Field field, boolean required, boolean eager) {
        super(field);
        this.declaringClass = field.getDeclaringClass();
        this.fieldName = field.getName();
        this.required = required;
        this.eager = eager;
    }

    //构造器5
    public DependencyDescriptor(DependencyDescriptor original) {
        super(original);
        this.declaringClass = original.declaringClass;
        this.methodName = original.methodName;
        this.parameterTypes = original.parameterTypes;
        this.parameterIndex = original.parameterIndex;
        this.fieldName = original.fieldName;
        this.containingClass = original.containingClass;
        this.required = original.required;
        this.eager = original.eager;
        this.nestingLevel = original.nestingLevel;
    }

    //是否需要
    public boolean isRequired() {
        return this.required;
    }

    /**
     * Return whether this dependency is 'eager' in the sense of
     * eagerly resolving potential target beans for type matching.
     */
    public boolean isEager() {
        return this.eager;
    }

    /**
     * Resolve the specified not-unique scenario: by default,
     * throwing a {@link NoUniqueBeanDefinitionException}.
     * <p>Subclasses may override this to select one of the instances or
     * to opt out with no result at all through returning {@code null}.
     *
     * @param type          the requested bean type
     * @param matchingBeans a map of bean names and corresponding bean
     *                      instances which have been pre-selected for the given type
     *                      (qualifiers etc already applied)
     * @return a bean instance to proceed with, or {@code null} for none
     * @throws BeansException in case of the not-unique scenario being fatal
     * @since 4.3
     */
    public Object resolveNotUnique(Class<?> type, Map<String, Object> matchingBeans) throws BeansException {
        throw new NoUniqueBeanDefinitionException(type, matchingBeans.keySet());
    }

    /**
     * Resolve a shortcut for this dependency against the given factory, for example
     * taking some pre-resolved information into account.
     * <p>The resolution algorithm will first attempt to resolve a shortcut through this
     * method before going into the regular type matching algorithm across all beans.
     * Subclasses may override this method to improve resolution performance based on
     * pre-cached information while still receiving {@link InjectionPoint} exposure etc.
     *
     * @param beanFactory the associated factory
     * @return the shortcut result if any, or {@code null} if none
     * @throws BeansException if the shortcut could not be obtained
     * @since 4.3.1
     */
    public Object resolveShortcut(BeanFactory beanFactory) throws BeansException {
        return null;
    }

    /**
     * Resolve the specified bean name, as a candidate result of the matching
     * algorithm for this dependency, to a bean instance from the given factory.
     * <p>The default implementation calls {@link BeanFactory#getBean(String)}.
     * Subclasses may provide additional arguments or other customizations.
     *
     * @param beanName     the bean name, as a candidate result for this dependency
     * @param requiredType the expected type of the bean (as an assertion)
     * @param beanFactory  the associated factory
     * @return the bean instance (never {@code null})
     * @throws BeansException if the bean could not be obtained
     * @see BeanFactory#getBean(String)
     * @since 4.3.2
     */
    public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory)
            throws BeansException {

        return beanFactory.getBean(beanName, requiredType);
    }


    /**
     * Increase this descriptor's nesting level.
     *
     * @see MethodParameter#increaseNestingLevel()
     */
    public void increaseNestingLevel() {
        this.nestingLevel++;
        this.resolvableType = null;
        if (this.methodParameter != null) {
            this.methodParameter.increaseNestingLevel();
        }
    }

    /**
     * Optionally set the concrete class that contains this dependency.
     * This may differ from the class that declares the parameter/field in that
     * it may be a subclass thereof, potentially substituting type variables.
     *
     * @since 4.0
     */
    public void setContainingClass(Class<?> containingClass) {
        this.containingClass = containingClass;
        this.resolvableType = null;
        if (this.methodParameter != null) {
            GenericTypeResolver.resolveParameterType(this.methodParameter, containingClass);
        }
    }

    /**
     * Build a ResolvableType object for the wrapped parameter/field.
     *
     * @since 4.0
     */
    public ResolvableType getResolvableType() {
        ResolvableType resolvableType = this.resolvableType;
        if (resolvableType == null) {
            resolvableType = (this.field != null ?
                    ResolvableType.forField(this.field, this.nestingLevel, this.containingClass) :
                    ResolvableType.forMethodParameter(this.methodParameter));
            this.resolvableType = resolvableType;
        }
        return resolvableType;
    }

    /**
     * Return whether a fallback match is allowed.
     * <p>This is {@code false} by default but may be overridden to return {@code true} in order
     * to suggest to a {@link AutowireCandidateResolver}
     * that a fallback match is acceptable as well.
     *
     * @since 4.0
     */
    public boolean fallbackMatchAllowed() {
        return false;
    }

    /**
     * Return a variant of this descriptor that is intended for a fallback match.
     *
     * @see #fallbackMatchAllowed()
     * @since 4.0
     */
    public DependencyDescriptor forFallbackMatch() {
        return new DependencyDescriptor(this) {
            @Override
            public boolean fallbackMatchAllowed() {
                return true;
            }
        };
    }

    /**
     * Initialize parameter name discovery for the underlying method parameter, if any.
     * <p>This method does not actually try to retrieve the parameter name at
     * this point; it just allows discovery to happen when the application calls
     * {@link #getDependencyName()} (if ever).
     */
    public void initParameterNameDiscovery(ParameterNameDiscoverer parameterNameDiscoverer) {
        if (this.methodParameter != null) {
            this.methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
        }
    }

    /**
     * Determine the name of the wrapped parameter/field.
     *
     * @return the declared name (never {@code null})
     */
    public String getDependencyName() {
        return (this.field != null ? this.field.getName() : this.methodParameter.getParameterName());
    }

    /**
     * Determine the declared (non-generic) type of the wrapped parameter/field.
     *
     * @return the declared type (never {@code null})
     */
    public Class<?> getDependencyType() {
        if (this.field != null) {
            if (this.nestingLevel > 1) {
                Type type = this.field.getGenericType();
                for (int i = 2; i <= this.nestingLevel; i++) {
                    if (type instanceof ParameterizedType) {
                        Type[] args = ((ParameterizedType) type).getActualTypeArguments();
                        type = args[args.length - 1];
                    }
                    // TODO: Object.class if unresolvable
                }
                if (type instanceof Class) {
                    return (Class<?>) type;
                } else if (type instanceof ParameterizedType) {
                    Type arg = ((ParameterizedType) type).getRawType();
                    if (arg instanceof Class) {
                        return (Class<?>) arg;
                    }
                }
                return Object.class;
            } else {
                return this.field.getType();
            }
        } else {
            return this.methodParameter.getNestedParameterType();
        }
    }

    /**
     * Determine the generic element type of the wrapped Collection parameter/field, if any.
     *
     * @return the generic type, or {@code null} if none
     * @deprecated as of 4.3.6, in favor of direct {@link ResolvableType} usage
     */
    @Deprecated
    public Class<?> getCollectionType() {
        return (this.field != null ?
                org.springframework.core.GenericCollectionTypeResolver.getCollectionFieldType(this.field, this.nestingLevel) :
                org.springframework.core.GenericCollectionTypeResolver.getCollectionParameterType(this.methodParameter));
    }

    /**
     * Determine the generic key type of the wrapped Map parameter/field, if any.
     *
     * @return the generic type, or {@code null} if none
     * @deprecated as of 4.3.6, in favor of direct {@link ResolvableType} usage
     */
    @Deprecated
    public Class<?> getMapKeyType() {
        return (this.field != null ?
                org.springframework.core.GenericCollectionTypeResolver.getMapKeyFieldType(this.field, this.nestingLevel) :
                org.springframework.core.GenericCollectionTypeResolver.getMapKeyParameterType(this.methodParameter));
    }

    /**
     * Determine the generic value type of the wrapped Map parameter/field, if any.
     *
     * @return the generic type, or {@code null} if none
     * @deprecated as of 4.3.6, in favor of direct {@link ResolvableType} usage
     */
    @Deprecated
    public Class<?> getMapValueType() {
        return (this.field != null ?
                org.springframework.core.GenericCollectionTypeResolver.getMapValueFieldType(this.field, this.nestingLevel) :
                org.springframework.core.GenericCollectionTypeResolver.getMapValueParameterType(this.methodParameter));
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        DependencyDescriptor otherDesc = (DependencyDescriptor) other;
        return (this.required == otherDesc.required && this.eager == otherDesc.eager &&
                this.nestingLevel == otherDesc.nestingLevel && this.containingClass == otherDesc.containingClass);
    }


    //---------------------------------------------------------------------
    // 序列化支持
    //---------------------------------------------------------------------

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        try {
            if (this.fieldName != null) {
                this.field = this.declaringClass.getDeclaredField(this.fieldName);
            } else {
                if (this.methodName != null) {
                    this.methodParameter = new MethodParameter(
                            this.declaringClass.getDeclaredMethod(this.methodName, this.parameterTypes), this.parameterIndex);
                } else {
                    this.methodParameter = new MethodParameter(
                            this.declaringClass.getDeclaredConstructor(this.parameterTypes), this.parameterIndex);
                }
                for (int i = 1; i < this.nestingLevel; i++) {
                    this.methodParameter.increaseNestingLevel();
                }
            }
        } catch (Throwable ex) {
            throw new IllegalStateException("Could not find original class structure", ex);
        }
    }

}
