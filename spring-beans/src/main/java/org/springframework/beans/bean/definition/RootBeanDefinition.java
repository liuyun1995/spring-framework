package org.springframework.beans.bean.definition;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.property.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

//根级Bean定义
@SuppressWarnings("serial")
public class RootBeanDefinition extends AbstractBeanDefinition {

	private BeanDefinitionHolder decoratedDefinition;      //Bean定义持有器
	private AnnotatedElement qualifiedElement;             //合格的元素
	boolean allowCaching = true;                           //是否允许缓存
	boolean isFactoryMethodUnique = false;                 //工厂方法是否唯一
	volatile ResolvableType targetType;                    //目标类型
	volatile Class<?> resolvedTargetType;                  //解析的目标类型
	volatile ResolvableType factoryMethodReturnType;       //工厂方法返回类型
	final Object constructorArgumentLock = new Object();   //构造器参数锁
	Object resolvedConstructorOrFactoryMethod;             //已解析的构造器或工厂方法
	boolean constructorArgumentsResolved = false;          //是否构造器参数已解析
	Object[] resolvedConstructorArguments;                 //已解析的构造参数集合
	Object[] preparedConstructorArguments;                 //准备好的构造参数
	final Object postProcessingLock = new Object();        //后置处理过程锁
	boolean postProcessed = false;                         //是否执行后置加工
	volatile Boolean beforeInstantiationResolved;          //是否在实例化之前解析
	private Set<Member> externallyManagedConfigMembers;    //额外管理的配置成员
	private Set<String> externallyManagedInitMethods;      //额外管理的初始化方法
	private Set<String> externallyManagedDestroyMethods;   //额外管理的销毁方法

	//构造器1
	public RootBeanDefinition() {
		super();
	}

	//构造器2
	public RootBeanDefinition(Class<?> beanClass) {
		super();
		setBeanClass(beanClass);
	}

	//构造器3
	public RootBeanDefinition(Class<?> beanClass, int autowireMode, boolean dependencyCheck) {
		super();
		setBeanClass(beanClass);
		setAutowireMode(autowireMode);
		if (dependencyCheck && getResolvedAutowireMode() != AUTOWIRE_CONSTRUCTOR) {
			setDependencyCheck(DEPENDENCY_CHECK_OBJECTS);
		}
	}

	//构造器4
	public RootBeanDefinition(Class<?> beanClass, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClass(beanClass);
	}

	//构造器5
	public RootBeanDefinition(String beanClassName) {
		setBeanClassName(beanClassName);
	}

	//构造器6
	public RootBeanDefinition(String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClassName(beanClassName);
	}

	//构造器7
	public RootBeanDefinition(RootBeanDefinition original) {
		super(original);
		this.decoratedDefinition = original.decoratedDefinition;
		this.qualifiedElement = original.qualifiedElement;
		this.allowCaching = original.allowCaching;
		this.isFactoryMethodUnique = original.isFactoryMethodUnique;
		this.targetType = original.targetType;
	}

	//构造器8
	RootBeanDefinition(BeanDefinition original) {
		super(original);
	}

	//获取父类名称
	@Override
	public String getParentName() {
		return null;
	}

	//设置父类名称
	@Override
	public void setParentName(String parentName) {
		if (parentName != null) {
			throw new IllegalArgumentException("Root bean cannot be changed into a child bean with parent reference");
		}
	}

	//设置装饰过的Bean定义
	public void setDecoratedDefinition(BeanDefinitionHolder decoratedDefinition) {
		this.decoratedDefinition = decoratedDefinition;
	}

	//获取装饰过的Bean定义
	public BeanDefinitionHolder getDecoratedDefinition() {
		return this.decoratedDefinition;
	}

	//设置有资格的元素
	public void setQualifiedElement(AnnotatedElement qualifiedElement) {
		this.qualifiedElement = qualifiedElement;
	}

	//获取有资格的元素
	public AnnotatedElement getQualifiedElement() {
		return this.qualifiedElement;
	}

	//设置目标类型
	public void setTargetType(ResolvableType targetType) {
		this.targetType = targetType;
	}

	//设置目标类型
	public void setTargetType(Class<?> targetType) {
		this.targetType = (targetType != null ? ResolvableType.forClass(targetType) : null);
	}

	//获取目标类型
	public Class<?> getTargetType() {
		if (this.resolvedTargetType != null) {
			return this.resolvedTargetType;
		}
		return (this.targetType != null ? this.targetType.resolve() : null);
	}

	//设置唯一工厂方法名
	public void setUniqueFactoryMethodName(String name) {
		Assert.hasText(name, "Factory method name must not be empty");
		setFactoryMethodName(name);
		this.isFactoryMethodUnique = true;
	}

	//是否是工厂方法
	public boolean isFactoryMethod(Method candidate) {
		return (candidate != null && candidate.getName().equals(getFactoryMethodName()));
	}

	//获取解析过的工厂方法
	public Method getResolvedFactoryMethod() {
		synchronized (this.constructorArgumentLock) {
			Object candidate = this.resolvedConstructorOrFactoryMethod;
			return (candidate instanceof Method ? (Method) candidate : null);
		}
	}

	//注册额外管理的配置成员
	public void registerExternallyManagedConfigMember(Member configMember) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedConfigMembers == null) {
				this.externallyManagedConfigMembers = new HashSet<Member>(1);
			}
			this.externallyManagedConfigMembers.add(configMember);
		}
	}

	//是否是额外管理的配置成员
	public boolean isExternallyManagedConfigMember(Member configMember) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedConfigMembers != null &&
					this.externallyManagedConfigMembers.contains(configMember));
		}
	}

	//注册额外管理的初始化方法
	public void registerExternallyManagedInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedInitMethods == null) {
				this.externallyManagedInitMethods = new HashSet<String>(1);
			}
			this.externallyManagedInitMethods.add(initMethod);
		}
	}

	//是否是额外管理的初始化方法
	public boolean isExternallyManagedInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedInitMethods != null &&
					this.externallyManagedInitMethods.contains(initMethod));
		}
	}

	//注册额外管理的销毁方法
	public void registerExternallyManagedDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedDestroyMethods == null) {
				this.externallyManagedDestroyMethods = new HashSet<String>(1);
			}
			this.externallyManagedDestroyMethods.add(destroyMethod);
		}
	}

	//是否是额外管理的销毁方法
	public boolean isExternallyManagedDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedDestroyMethods != null &&
					this.externallyManagedDestroyMethods.contains(destroyMethod));
		}
	}

	//克隆Bean定义
	@Override
	public RootBeanDefinition cloneBeanDefinition() {
		return new RootBeanDefinition(this);
	}

	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof RootBeanDefinition && super.equals(other)));
	}

	@Override
	public String toString() {
		return "Root bean: " + super.toString();
	}

}
