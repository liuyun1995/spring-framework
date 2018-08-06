package org.springframework.beans.factory.xml.parser;

import org.springframework.beans.bean.definition.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.xml.reader.BeanDefinitionReaderUtils;
import org.springframework.beans.bean.registry.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlReaderContext;

import java.util.Stack;

//解析上下文
public final class ParserContext {

	private final XmlReaderContext readerContext;          //xml阅读上下文
	private final BeanDefinitionParserDelegate delegate;   //Bean定义解析器修饰器
	private BeanDefinition containingBeanDefinition;       //Bean定义
	//组件定义集合
	private final Stack<ComponentDefinition> containingComponents = new Stack<ComponentDefinition>();

	//构造器
	public ParserContext(XmlReaderContext readerContext, BeanDefinitionParserDelegate delegate) {
		this.readerContext = readerContext;
		this.delegate = delegate;
	}

	//构造器
	public ParserContext(XmlReaderContext readerContext, BeanDefinitionParserDelegate delegate,
			BeanDefinition containingBeanDefinition) {

		this.readerContext = readerContext;
		this.delegate = delegate;
		this.containingBeanDefinition = containingBeanDefinition;
	}

	//获取xml解析上下文
	public final XmlReaderContext getReaderContext() {
		return this.readerContext;
	}

	//获取Bean定义注册器
	public final BeanDefinitionRegistry getRegistry() {
		return this.readerContext.getRegistry();
	}

	//获取Bean定义解析器装饰器
	public final BeanDefinitionParserDelegate getDelegate() {
		return this.delegate;
	}

	//获取包含的Bean定义
	public final BeanDefinition getContainingBeanDefinition() {
		return this.containingBeanDefinition;
	}

	//是否是嵌套
	public final boolean isNested() {
		return (this.containingBeanDefinition != null);
	}

	//是否默认懒加载
	public boolean isDefaultLazyInit() {
		return BeanDefinitionParserDelegate.TRUE_VALUE.equals(this.delegate.getDefaults().getLazyInit());
	}

	//获取额外xml资源
	public Object extractSource(Object sourceCandidate) {
		return this.readerContext.extractSource(sourceCandidate);
	}

	//获取包含的组件
	public CompositeComponentDefinition getContainingComponent() {
		//若组件定义集合不为空，则获取最后一个元素，否则返回null
		return (!this.containingComponents.isEmpty()
				? (CompositeComponentDefinition) this.containingComponents.lastElement()
				: null);
	}

	//放入一个组件
	public void pushContainingComponent(CompositeComponentDefinition containingComponent) {
		this.containingComponents.push(containingComponent);
	}

	//弹出一个组件
	public CompositeComponentDefinition popContainingComponent() {
		return (CompositeComponentDefinition) this.containingComponents.pop();
	}

	//弹出并注册包含的组件
	public void popAndRegisterContainingComponent() {
		registerComponent(popContainingComponent());
	}

	//注册组件定义
	public void registerComponent(ComponentDefinition component) {
		CompositeComponentDefinition containingComponent = getContainingComponent();
		if (containingComponent != null) {
			containingComponent.addNestedComponent(component);
		} else {
			this.readerContext.fireComponentRegistered(component);
		}
	}

	//注册Bean组件
	public void registerBeanComponent(BeanComponentDefinition component) {
		BeanDefinitionReaderUtils.registerBeanDefinition(component, getRegistry());
		registerComponent(component);
	}

}
