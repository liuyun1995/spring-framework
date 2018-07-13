package org.springframework.context;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.support.ResourcePatternResolver;

//应用上下文接口
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {

	//获取应用上下文ID
	String getId();

	//获取应用名称
	String getApplicationName();

	//获取上下文别名
	String getDisplayName();
	
	//获取上下文首次加载的时间戳
	long getStartupDate();

	//获取父级上下文
	ApplicationContext getParent();

	//获取AutowireCapableBeanFactory
	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

}
