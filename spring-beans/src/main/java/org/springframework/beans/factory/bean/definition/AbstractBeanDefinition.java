package org.springframework.beans.factory.bean.definition;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanMetadataAttributeAccessor;
import org.springframework.beans.factory.support.*;
import org.springframework.beans.factory.support.autowire.AutowireCandidateQualifier;
import org.springframework.beans.property.MutablePropertyValues;
import org.springframework.beans.factory.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

//抽象Bean定义
@SuppressWarnings("serial")
public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor
        implements BeanDefinition, Cloneable {

    public static final String SCOPE_DEFAULT = "";
    public static final int AUTOWIRE_NO = AutowireCapableBeanFactory.AUTOWIRE_NO;
    public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;
    public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;
    public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;
    @Deprecated
    public static final int AUTOWIRE_AUTODETECT = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;

    public static final int DEPENDENCY_CHECK_NONE = 0;
    public static final int DEPENDENCY_CHECK_OBJECTS = 1;
    public static final int DEPENDENCY_CHECK_SIMPLE = 2;
    public static final int DEPENDENCY_CHECK_ALL = 3;

    public static final String INFER_METHOD = "(inferred)";                    //推断的方法名
    private volatile Object beanClass;                                         //Bean的类型
    private String scope = SCOPE_DEFAULT;                                      //Bean的范围
    private boolean abstractFlag = false;                                      //抽象标志
    private boolean lazyInit = false;                                          //是否懒加载
    private int autowireMode = AUTOWIRE_NO;                                    //自动装配模式
    private int dependencyCheck = DEPENDENCY_CHECK_NONE;                       //依赖检查模式
    private String[] dependsOn;                                                //依赖对象名称集合
    private boolean autowireCandidate = true;                                  //是否自动装配候选
    private boolean primary = false;                                           //是否是主要候选者
    private final Map<String, org.springframework.beans.factory.support.autowire.AutowireCandidateQualifier> qualifiers =         //自动装配候选者
            new LinkedHashMap<String, org.springframework.beans.factory.support.autowire.AutowireCandidateQualifier>(0);
    private boolean nonPublicAccessAllowed = true;                             //是否允许访问非公共属性
    private boolean lenientConstructorResolution = true;
    private String factoryBeanName;                                            //工厂Bean名称
    private String factoryMethodName;                                          //工厂方法名称
    private ConstructorArgumentValues constructorArgumentValues;               //构造器参数值
    private MutablePropertyValues propertyValues;                              //属性值
    private MethodOverrides methodOverrides = new MethodOverrides();           //方法覆盖集合
    private String initMethodName;                                             //指定的初始化方法
    private String destroyMethodName;                                          //指定的销毁方法
    private boolean enforceInitMethod = true;                                  //是否执行初始化方法
    private boolean enforceDestroyMethod = true;                               //是否执行销毁方法
    private boolean synthetic = false;                                         //Bean是否由程序生成
    private int role = BeanDefinition.ROLE_APPLICATION;                        //应用角色
    private String description;                                                //描述
    private Resource resource;                                                 //资源文件

    //构造器
    protected AbstractBeanDefinition() {
        this(null, null);
    }

    //构造器
    protected AbstractBeanDefinition(ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
        setConstructorArgumentValues(cargs);
        setPropertyValues(pvs);
    }

    //深度复制原有Bean定义
    protected AbstractBeanDefinition(BeanDefinition original) {
        setParentName(original.getParentName());
        setBeanClassName(original.getBeanClassName());
        setScope(original.getScope());
        setAbstract(original.isAbstract());
        setLazyInit(original.isLazyInit());
        setFactoryBeanName(original.getFactoryBeanName());
        setFactoryMethodName(original.getFactoryMethodName());
        setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
        setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
        setRole(original.getRole());
        setSource(original.getSource());
        copyAttributesFrom(original);

        if (original instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
            if (originalAbd.hasBeanClass()) {
                setBeanClass(originalAbd.getBeanClass());
            }
            setAutowireMode(originalAbd.getAutowireMode());
            setDependencyCheck(originalAbd.getDependencyCheck());
            setDependsOn(originalAbd.getDependsOn());
            setAutowireCandidate(originalAbd.isAutowireCandidate());
            setPrimary(originalAbd.isPrimary());
            copyQualifiersFrom(originalAbd);
            setNonPublicAccessAllowed(originalAbd.isNonPublicAccessAllowed());
            setLenientConstructorResolution(originalAbd.isLenientConstructorResolution());
            setMethodOverrides(new MethodOverrides(originalAbd.getMethodOverrides()));
            setInitMethodName(originalAbd.getInitMethodName());
            setEnforceInitMethod(originalAbd.isEnforceInitMethod());
            setDestroyMethodName(originalAbd.getDestroyMethodName());
            setEnforceDestroyMethod(originalAbd.isEnforceDestroyMethod());
            setSynthetic(originalAbd.isSynthetic());
            setResource(originalAbd.getResource());
        } else {
            setResourceDescription(original.getResourceDescription());
        }
    }

    //覆盖当前Bean定义
    public void overrideFrom(BeanDefinition other) {
        if (StringUtils.hasLength(other.getBeanClassName())) {
            setBeanClassName(other.getBeanClassName());
        }
        if (StringUtils.hasLength(other.getScope())) {
            setScope(other.getScope());
        }
        setAbstract(other.isAbstract());
        setLazyInit(other.isLazyInit());
        if (StringUtils.hasLength(other.getFactoryBeanName())) {
            setFactoryBeanName(other.getFactoryBeanName());
        }
        if (StringUtils.hasLength(other.getFactoryMethodName())) {
            setFactoryMethodName(other.getFactoryMethodName());
        }
        getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
        getPropertyValues().addPropertyValues(other.getPropertyValues());
        setRole(other.getRole());
        setSource(other.getSource());
        copyAttributesFrom(other);

        if (other instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition otherAbd = (AbstractBeanDefinition) other;
            if (otherAbd.hasBeanClass()) {
                setBeanClass(otherAbd.getBeanClass());
            }
            setAutowireMode(otherAbd.getAutowireMode());
            setDependencyCheck(otherAbd.getDependencyCheck());
            setDependsOn(otherAbd.getDependsOn());
            setAutowireCandidate(otherAbd.isAutowireCandidate());
            setPrimary(otherAbd.isPrimary());
            copyQualifiersFrom(otherAbd);
            setNonPublicAccessAllowed(otherAbd.isNonPublicAccessAllowed());
            setLenientConstructorResolution(otherAbd.isLenientConstructorResolution());
            getMethodOverrides().addOverrides(otherAbd.getMethodOverrides());
            if (StringUtils.hasLength(otherAbd.getInitMethodName())) {
                setInitMethodName(otherAbd.getInitMethodName());
                setEnforceInitMethod(otherAbd.isEnforceInitMethod());
            }
            if (otherAbd.getDestroyMethodName() != null) {
                setDestroyMethodName(otherAbd.getDestroyMethodName());
                setEnforceDestroyMethod(otherAbd.isEnforceDestroyMethod());
            }
            setSynthetic(otherAbd.isSynthetic());
            setResource(otherAbd.getResource());
        } else {
            setResourceDescription(other.getResourceDescription());
        }
    }

    //采用默认设置
    public void applyDefaults(BeanDefinitionDefaults defaults) {
        setLazyInit(defaults.isLazyInit());
        setAutowireMode(defaults.getAutowireMode());
        setDependencyCheck(defaults.getDependencyCheck());
        setInitMethodName(defaults.getInitMethodName());
        setEnforceInitMethod(false);
        setDestroyMethodName(defaults.getDestroyMethodName());
        setEnforceDestroyMethod(false);
    }

    //设置Bean类型名
    @Override
    public void setBeanClassName(String beanClassName) {
        this.beanClass = beanClassName;
    }

    //获取Bean类型名
    @Override
    public String getBeanClassName() {
        Object beanClassObject = this.beanClass;
        if (beanClassObject instanceof Class) {
            return ((Class<?>) beanClassObject).getName();
        } else {
            return (String) beanClassObject;
        }
    }

    //设置Bean类型
    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    //获取Bean类型
    public Class<?> getBeanClass() throws IllegalStateException {
        Object beanClassObject = this.beanClass;
        if (beanClassObject == null) {
            throw new IllegalStateException("No bean class specified on bean definition");
        }
        if (!(beanClassObject instanceof Class)) {
            throw new IllegalStateException(
                    "Bean class name [" + beanClassObject + "] has not been resolved into an actual Class");
        }
        return (Class<?>) beanClassObject;
    }

    //是否存在Bean类型
    public boolean hasBeanClass() {
        return (this.beanClass instanceof Class);
    }

    //解析Bean类型
    public Class<?> resolveBeanClass(ClassLoader classLoader) throws ClassNotFoundException {
        String className = getBeanClassName();
        if (className == null) {
            return null;
        }
        Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
        this.beanClass = resolvedClass;
        return resolvedClass;
    }

    //设置Bean的范围
    @Override
    public void setScope(String scope) {
        this.scope = scope;
    }

    //获取Bean的范围
    @Override
    public String getScope() {
        return this.scope;
    }

    //是否是单例
    @Override
    public boolean isSingleton() {
        return SCOPE_SINGLETON.equals(scope) || SCOPE_DEFAULT.equals(scope);
    }

    //是否是原型
    @Override
    public boolean isPrototype() {
        return SCOPE_PROTOTYPE.equals(scope);
    }

    //设置是否是抽象Bean
    public void setAbstract(boolean abstractFlag) {
        this.abstractFlag = abstractFlag;
    }

    //是否是抽象Bean
    @Override
    public boolean isAbstract() {
        return this.abstractFlag;
    }

    //设置是否懒加载
    @Override
    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    //是否懒加载
    @Override
    public boolean isLazyInit() {
        return this.lazyInit;
    }

    //设置自动装配模式
    public void setAutowireMode(int autowireMode) {
        this.autowireMode = autowireMode;
    }

    //获取自动装配模式
    public int getAutowireMode() {
        return this.autowireMode;
    }

    //获取解析后的自动装配模式
    public int getResolvedAutowireMode() {
        if (this.autowireMode == AUTOWIRE_AUTODETECT) {
            Constructor<?>[] constructors = getBeanClass().getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterTypes().length == 0) {
                    return AUTOWIRE_BY_TYPE;
                }
            }
            return AUTOWIRE_CONSTRUCTOR;
        } else {
            return this.autowireMode;
        }
    }

    //设置依赖检查模式
    public void setDependencyCheck(int dependencyCheck) {
        this.dependencyCheck = dependencyCheck;
    }

    //获取依赖检查模式
    public int getDependencyCheck() {
        return this.dependencyCheck;
    }

    //设置依赖Bean名称集合
    @Override
    public void setDependsOn(String... dependsOn) {
        this.dependsOn = dependsOn;
    }

    //获取依赖Bean名称集合
    @Override
    public String[] getDependsOn() {
        return this.dependsOn;
    }

    //设置是否自动装配候选
    @Override
    public void setAutowireCandidate(boolean autowireCandidate) {
        this.autowireCandidate = autowireCandidate;
    }

    //是否自动装配候选
    @Override
    public boolean isAutowireCandidate() {
        return this.autowireCandidate;
    }

    //设置是否主要候选者
    @Override
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    //是否主要候选者
    @Override
    public boolean isPrimary() {
        return this.primary;
    }

    //添加自动装配合格者
    public void addQualifier(org.springframework.beans.factory.support.autowire.AutowireCandidateQualifier qualifier) {
        this.qualifiers.put(qualifier.getTypeName(), qualifier);
    }

    //判断对应类型是否合格
    public boolean hasQualifier(String typeName) {
        return this.qualifiers.keySet().contains(typeName);
    }

    //获取对应类型的合格者
    public org.springframework.beans.factory.support.autowire.AutowireCandidateQualifier getQualifier(String typeName) {
        return this.qualifiers.get(typeName);
    }

    //获取所有自动装配合格者
    public Set<org.springframework.beans.factory.support.autowire.AutowireCandidateQualifier> getQualifiers() {
        return new LinkedHashSet<AutowireCandidateQualifier>(this.qualifiers.values());
    }

    //复制自动装配合格者
    public void copyQualifiersFrom(AbstractBeanDefinition source) {
        Assert.notNull(source, "Source must not be null");
        this.qualifiers.putAll(source.qualifiers);
    }

    //设置是否允许访问非公共属性
    public void setNonPublicAccessAllowed(boolean nonPublicAccessAllowed) {
        this.nonPublicAccessAllowed = nonPublicAccessAllowed;
    }

    //是否允许访问非公共属性
    public boolean isNonPublicAccessAllowed() {
        return this.nonPublicAccessAllowed;
    }

    /**
     * Specify whether to resolve constructors in lenient mode ({@code true},
     * which is the default) or to switch to strict resolution (throwing an exception
     * in case of ambiguous constructors that all match when converting the arguments,
     * whereas lenient mode would use the one with the 'closest' type matches).
     */
    public void setLenientConstructorResolution(boolean lenientConstructorResolution) {
        this.lenientConstructorResolution = lenientConstructorResolution;
    }

    /**
     * Return whether to resolve constructors in lenient mode or in strict mode.
     */
    public boolean isLenientConstructorResolution() {
        return this.lenientConstructorResolution;
    }

    //设置工厂Bean名称
    @Override
    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    //获取工厂Bean名称
    @Override
    public String getFactoryBeanName() {
        return this.factoryBeanName;
    }

    //设置工厂方法名称
    @Override
    public void setFactoryMethodName(String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }

    //获取工厂方法名称
    @Override
    public String getFactoryMethodName() {
        return this.factoryMethodName;
    }

    //设置构造参数值
    public void setConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues) {
        this.constructorArgumentValues =
                (constructorArgumentValues != null ? constructorArgumentValues : new ConstructorArgumentValues());
    }

    //获取构造参数值
    @Override
    public ConstructorArgumentValues getConstructorArgumentValues() {
        return this.constructorArgumentValues;
    }

    //是否有构造参数值
    public boolean hasConstructorArgumentValues() {
        return !this.constructorArgumentValues.isEmpty();
    }

    //设置属性值
    public void setPropertyValues(MutablePropertyValues propertyValues) {
        this.propertyValues = (propertyValues != null ? propertyValues : new MutablePropertyValues());
    }

    //获取属性值
    @Override
    public MutablePropertyValues getPropertyValues() {
        return this.propertyValues;
    }

    //设置方法覆盖集合
    public void setMethodOverrides(MethodOverrides methodOverrides) {
        this.methodOverrides = (methodOverrides != null ? methodOverrides : new MethodOverrides());
    }

    //获取方法覆盖集合
    public MethodOverrides getMethodOverrides() {
        return this.methodOverrides;
    }

    //设置初始化方法名
    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    //获取初始化方法名
    public String getInitMethodName() {
        return this.initMethodName;
    }

    //设置是否执行初始化方法
    public void setEnforceInitMethod(boolean enforceInitMethod) {
        this.enforceInitMethod = enforceInitMethod;
    }

    //是否执行初始化方法
    public boolean isEnforceInitMethod() {
        return this.enforceInitMethod;
    }

    //设置销毁方法名
    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }

    //获取销毁方法名
    public String getDestroyMethodName() {
        return this.destroyMethodName;
    }

    //设置是否执行销毁方法
    public void setEnforceDestroyMethod(boolean enforceDestroyMethod) {
        this.enforceDestroyMethod = enforceDestroyMethod;
    }

    //是否执行销毁方法
    public boolean isEnforceDestroyMethod() {
        return this.enforceDestroyMethod;
    }

    //设置是否是程序合成的
    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }

    //是否是程序合成的
    public boolean isSynthetic() {
        return this.synthetic;
    }

    //设置角色
    public void setRole(int role) {
        this.role = role;
    }

    //获取角色
    @Override
    public int getRole() {
        return this.role;
    }

    //设置描述符
    public void setDescription(String description) {
        this.description = description;
    }

    //获取描述符
    @Override
    public String getDescription() {
        return this.description;
    }

    //设置资源文件
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    //获取资源文件
    public Resource getResource() {
        return this.resource;
    }

    //设置资源描述符
    public void setResourceDescription(String resourceDescription) {
        this.resource = new DescriptiveResource(resourceDescription);
    }

    //获取资源描述符
    @Override
    public String getResourceDescription() {
        return (this.resource != null ? this.resource.getDescription() : null);
    }

    //设置初始Bean定义
    public void setOriginatingBeanDefinition(BeanDefinition originatingBd) {
        this.resource = new BeanDefinitionResource(originatingBd);
    }

    //获取初始Bean定义
    @Override
    public BeanDefinition getOriginatingBeanDefinition() {
        return (this.resource instanceof BeanDefinitionResource ?
                ((BeanDefinitionResource) this.resource).getBeanDefinition() : null);
    }

    //验证方法
    public void validate() throws BeanDefinitionValidationException {
        if (!getMethodOverrides().isEmpty() && getFactoryMethodName() != null) {
            throw new BeanDefinitionValidationException(
                    "Cannot combine static factory method with method overrides: " +
                            "the static factory method must create the instance");
        }
        if (hasBeanClass()) {
            prepareMethodOverrides();
        }
    }

    //准备方法覆盖
    public void prepareMethodOverrides() throws BeanDefinitionValidationException {
        MethodOverrides methodOverrides = getMethodOverrides();
        if (!methodOverrides.isEmpty()) {
            Set<MethodOverride> overrides = methodOverrides.getOverrides();
            synchronized (overrides) {
                for (MethodOverride mo : overrides) {
                    prepareMethodOverride(mo);
                }
            }
        }
    }

    //准备方法覆盖
    protected void prepareMethodOverride(MethodOverride mo) throws BeanDefinitionValidationException {
        int count = ClassUtils.getMethodCountForName(getBeanClass(), mo.getMethodName());
        if (count == 0) {
            throw new BeanDefinitionValidationException(
                    "Invalid method override: no method with name '" + mo.getMethodName() +
                            "' on class [" + getBeanClassName() + "]");
        } else if (count == 1) {
            // Mark override as not overloaded, to avoid the overhead of arg type checking.
            mo.setOverloaded(false);
        }
    }

    //克隆方法
    @Override
    public Object clone() {
        return cloneBeanDefinition();
    }

    //克隆Bean定义
    public abstract AbstractBeanDefinition cloneBeanDefinition();

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AbstractBeanDefinition)) {
            return false;
        }

        AbstractBeanDefinition that = (AbstractBeanDefinition) other;

        if (!ObjectUtils.nullSafeEquals(getBeanClassName(), that.getBeanClassName())) return false;
        if (!ObjectUtils.nullSafeEquals(this.scope, that.scope)) return false;
        if (this.abstractFlag != that.abstractFlag) return false;
        if (this.lazyInit != that.lazyInit) return false;

        if (this.autowireMode != that.autowireMode) return false;
        if (this.dependencyCheck != that.dependencyCheck) return false;
        if (!Arrays.equals(this.dependsOn, that.dependsOn)) return false;
        if (this.autowireCandidate != that.autowireCandidate) return false;
        if (!ObjectUtils.nullSafeEquals(this.qualifiers, that.qualifiers)) return false;
        if (this.primary != that.primary) return false;

        if (this.nonPublicAccessAllowed != that.nonPublicAccessAllowed) return false;
        if (this.lenientConstructorResolution != that.lenientConstructorResolution) return false;
        if (!ObjectUtils.nullSafeEquals(this.constructorArgumentValues, that.constructorArgumentValues)) return false;
        if (!ObjectUtils.nullSafeEquals(this.propertyValues, that.propertyValues)) return false;
        if (!ObjectUtils.nullSafeEquals(this.methodOverrides, that.methodOverrides)) return false;

        if (!ObjectUtils.nullSafeEquals(this.factoryBeanName, that.factoryBeanName)) return false;
        if (!ObjectUtils.nullSafeEquals(this.factoryMethodName, that.factoryMethodName)) return false;
        if (!ObjectUtils.nullSafeEquals(this.initMethodName, that.initMethodName)) return false;
        if (this.enforceInitMethod != that.enforceInitMethod) return false;
        if (!ObjectUtils.nullSafeEquals(this.destroyMethodName, that.destroyMethodName)) return false;
        if (this.enforceDestroyMethod != that.enforceDestroyMethod) return false;

        if (this.synthetic != that.synthetic) return false;
        if (this.role != that.role) return false;

        return super.equals(other);
    }

    @Override
    public int hashCode() {
        int hashCode = ObjectUtils.nullSafeHashCode(getBeanClassName());
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.scope);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.constructorArgumentValues);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.propertyValues);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryBeanName);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryMethodName);
        hashCode = 29 * hashCode + super.hashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("class [");
        sb.append(getBeanClassName()).append("]");
        sb.append("; scope=").append(this.scope);
        sb.append("; abstract=").append(this.abstractFlag);
        sb.append("; lazyInit=").append(this.lazyInit);
        sb.append("; autowireMode=").append(this.autowireMode);
        sb.append("; dependencyCheck=").append(this.dependencyCheck);
        sb.append("; autowireCandidate=").append(this.autowireCandidate);
        sb.append("; primary=").append(this.primary);
        sb.append("; factoryBeanName=").append(this.factoryBeanName);
        sb.append("; factoryMethodName=").append(this.factoryMethodName);
        sb.append("; initMethodName=").append(this.initMethodName);
        sb.append("; destroyMethodName=").append(this.destroyMethodName);
        if (this.resource != null) {
            sb.append("; defined in ").append(this.resource.getDescription());
        }
        return sb.toString();
    }

}
