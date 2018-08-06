package org.springframework.beans.bean.definition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.exception.BeanDefinitionStoreException;
import org.springframework.beans.exception.NoSuchBeanDefinitionException;
import org.springframework.core.SimpleAliasRegistry;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

//简单Bean定义注册器
public class SimpleBeanDefinitionRegistry extends SimpleAliasRegistry implements BeanDefinitionRegistry {

	//Bean定义映射器
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>(64);

	//注册Bean定义
	@Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {
		Assert.hasText(beanName, "'beanName' must not be empty");
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		this.beanDefinitionMap.put(beanName, beanDefinition);
	}

	//注册Bean定义
	@Override
	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		if (this.beanDefinitionMap.remove(beanName) == null) {
			throw new NoSuchBeanDefinitionException(beanName);
		}
	}

	//获取Bean定义
	@Override
	public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		BeanDefinition bd = this.beanDefinitionMap.get(beanName);
		if (bd == null) {
			throw new NoSuchBeanDefinitionException(beanName);
		}
		return bd;
	}

	//是否包含Bean定义
	@Override
	public boolean containsBeanDefinition(String beanName) {
		return this.beanDefinitionMap.containsKey(beanName);
	}

	//获取所有Bean名称
	@Override
	public String[] getBeanDefinitionNames() {
		return StringUtils.toStringArray(this.beanDefinitionMap.keySet());
	}

	//获取Bean定义数量
	@Override
	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	//是否Bean名称已被使用
	@Override
	public boolean isBeanNameInUse(String beanName) {
		return isAlias(beanName) || containsBeanDefinition(beanName);
	}

}
