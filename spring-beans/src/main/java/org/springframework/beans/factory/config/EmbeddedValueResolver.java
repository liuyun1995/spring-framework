package org.springframework.beans.factory.config;

import org.springframework.util.StringValueResolver;

//嵌套值解析器
public class EmbeddedValueResolver implements StringValueResolver {

	private final BeanExpressionContext exprContext;      //Bean表达式上下文
	private final BeanExpressionResolver exprResolver;    //Bean表达式解析器

	//构造器
	public EmbeddedValueResolver(ConfigurableBeanFactory beanFactory) {
		this.exprContext = new BeanExpressionContext(beanFactory, null);
		this.exprResolver = beanFactory.getBeanExpressionResolver();
	}

	//解析字符串值
	@Override
	public String resolveStringValue(String strVal) {
		String value = this.exprContext.getBeanFactory().resolveEmbeddedValue(strVal);
		if (this.exprResolver != null && value != null) {
			Object evaluated = this.exprResolver.evaluate(value, this.exprContext);
			value = (evaluated != null ? evaluated.toString() : null);
		}
		return value;
	}

}
