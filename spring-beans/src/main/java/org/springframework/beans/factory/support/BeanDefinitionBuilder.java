package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.util.ObjectUtils;

//Bean定义构建器
public class BeanDefinitionBuilder {

    public static BeanDefinitionBuilder genericBeanDefinition() {
        BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
        builder.beanDefinition = new GenericBeanDefinition();
        return builder;
    }

    public static BeanDefinitionBuilder genericBeanDefinition(Class<?> beanClass) {
        BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
        builder.beanDefinition = new GenericBeanDefinition();
        builder.beanDefinition.setBeanClass(beanClass);
        return builder;
    }

    public static BeanDefinitionBuilder genericBeanDefinition(String beanClassName) {
        BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
        builder.beanDefinition = new GenericBeanDefinition();
        builder.beanDefinition.setBeanClassName(beanClassName);
        return builder;
    }

    public static BeanDefinitionBuilder rootBeanDefinition(Class<?> beanClass) {
        return rootBeanDefinition(beanClass, null);
    }

    public static BeanDefinitionBuilder rootBeanDefinition(Class<?> beanClass, String factoryMethodName) {
        BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
        builder.beanDefinition = new RootBeanDefinition();
        builder.beanDefinition.setBeanClass(beanClass);
        builder.beanDefinition.setFactoryMethodName(factoryMethodName);
        return builder;
    }

    public static BeanDefinitionBuilder rootBeanDefinition(String beanClassName) {
        return rootBeanDefinition(beanClassName, null);
    }

    public static BeanDefinitionBuilder rootBeanDefinition(String beanClassName, String factoryMethodName) {
        BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
        builder.beanDefinition = new RootBeanDefinition();
        builder.beanDefinition.setBeanClassName(beanClassName);
        builder.beanDefinition.setFactoryMethodName(factoryMethodName);
        return builder;
    }

    public static BeanDefinitionBuilder childBeanDefinition(String parentName) {
        BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
        builder.beanDefinition = new ChildBeanDefinition(parentName);
        return builder;
    }

    private AbstractBeanDefinition beanDefinition;

    private int constructorArgIndex;

    private BeanDefinitionBuilder() {
    }

    public AbstractBeanDefinition getRawBeanDefinition() {
        return this.beanDefinition;
    }

    public AbstractBeanDefinition getBeanDefinition() {
        this.beanDefinition.validate();
        return this.beanDefinition;
    }

    public BeanDefinitionBuilder setParentName(String parentName) {
        this.beanDefinition.setParentName(parentName);
        return this;
    }

    public BeanDefinitionBuilder setFactoryMethod(String factoryMethod) {
        this.beanDefinition.setFactoryMethodName(factoryMethod);
        return this;
    }

    public BeanDefinitionBuilder setFactoryMethodOnBean(String factoryMethod, String factoryBean) {
        this.beanDefinition.setFactoryMethodName(factoryMethod);
        this.beanDefinition.setFactoryBeanName(factoryBean);
        return this;
    }

    /**
     * Add an indexed constructor arg value. The current index is tracked internally
     * and all additions are at the present point.
     *
     * @deprecated since Spring 2.5, in favor of {@link #addConstructorArgValue}.
     * This variant just remains around for Spring Security 2.x compatibility.
     */
    @Deprecated
    public BeanDefinitionBuilder addConstructorArg(Object value) {
        return addConstructorArgValue(value);
    }

    /**
     * Add an indexed constructor arg value. The current index is tracked internally
     * and all additions are at the present point.
     */
    public BeanDefinitionBuilder addConstructorArgValue(Object value) {
        this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(
                this.constructorArgIndex++, value);
        return this;
    }

    /**
     * Add a reference to a named bean as a constructor arg.
     *
     * @see #addConstructorArgValue(Object)
     */
    public BeanDefinitionBuilder addConstructorArgReference(String beanName) {
        this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(
                this.constructorArgIndex++, new RuntimeBeanReference(beanName));
        return this;
    }

    /**
     * Add the supplied property value under the given name.
     */
    public BeanDefinitionBuilder addPropertyValue(String name, Object value) {
        this.beanDefinition.getPropertyValues().add(name, value);
        return this;
    }

    /**
     * Add a reference to the specified bean name under the property specified.
     *
     * @param name     the name of the property to add the reference to
     * @param beanName the name of the bean being referenced
     */
    public BeanDefinitionBuilder addPropertyReference(String name, String beanName) {
        this.beanDefinition.getPropertyValues().add(name, new RuntimeBeanReference(beanName));
        return this;
    }

    /**
     * Set the init method for this definition.
     */
    public BeanDefinitionBuilder setInitMethodName(String methodName) {
        this.beanDefinition.setInitMethodName(methodName);
        return this;
    }

    /**
     * Set the destroy method for this definition.
     */
    public BeanDefinitionBuilder setDestroyMethodName(String methodName) {
        this.beanDefinition.setDestroyMethodName(methodName);
        return this;
    }


    /**
     * Set the scope of this definition.
     *
     * @see org.springframework.beans.factory.config.BeanDefinition#SCOPE_SINGLETON
     * @see org.springframework.beans.factory.config.BeanDefinition#SCOPE_PROTOTYPE
     */
    public BeanDefinitionBuilder setScope(String scope) {
        this.beanDefinition.setScope(scope);
        return this;
    }

    /**
     * Set whether or not this definition is abstract.
     */
    public BeanDefinitionBuilder setAbstract(boolean flag) {
        this.beanDefinition.setAbstract(flag);
        return this;
    }

    /**
     * Set whether beans for this definition should be lazily initialized or not.
     */
    public BeanDefinitionBuilder setLazyInit(boolean lazy) {
        this.beanDefinition.setLazyInit(lazy);
        return this;
    }

    /**
     * Set the autowire mode for this definition.
     */
    public BeanDefinitionBuilder setAutowireMode(int autowireMode) {
        beanDefinition.setAutowireMode(autowireMode);
        return this;
    }

    /**
     * Set the depency check mode for this definition.
     */
    public BeanDefinitionBuilder setDependencyCheck(int dependencyCheck) {
        beanDefinition.setDependencyCheck(dependencyCheck);
        return this;
    }

    /**
     * Append the specified bean name to the list of beans that this definition
     * depends on.
     */
    public BeanDefinitionBuilder addDependsOn(String beanName) {
        if (this.beanDefinition.getDependsOn() == null) {
            this.beanDefinition.setDependsOn(beanName);
        } else {
            String[] added = ObjectUtils.addObjectToArray(this.beanDefinition.getDependsOn(), beanName);
            this.beanDefinition.setDependsOn(added);
        }
        return this;
    }

    /**
     * Set the role of this definition.
     */
    public BeanDefinitionBuilder setRole(int role) {
        this.beanDefinition.setRole(role);
        return this;
    }

}
