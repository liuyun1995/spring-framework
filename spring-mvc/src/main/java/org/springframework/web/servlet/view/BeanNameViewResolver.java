package org.springframework.web.servlet.view;

import java.util.Locale;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

//Bean名称视图解析器
public class BeanNameViewResolver extends WebApplicationObjectSupport implements ViewResolver, Ordered {

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	//设置排序
	public void setOrder(int order) {
		this.order = order;
	}

	//获取排序
	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public View resolveViewName(String viewName, Locale locale) throws BeansException {
		//获取应用上下文
		ApplicationContext context = getApplicationContext();
		//若不包含该Bean
		if (!context.containsBean(viewName)) {
			if (logger.isDebugEnabled()) {
				logger.debug("No matching bean found for view name '" + viewName + "'");
			}
			return null;
		}
		//若类型不匹配
		if (!context.isTypeMatch(viewName, View.class)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Found matching bean for view name '" + viewName +
						"' - to be ignored since it does not implement View");
			}
			return null;
		}
		//获取指定名称和类型的Bean
		return context.getBean(viewName, View.class);
	}

}
