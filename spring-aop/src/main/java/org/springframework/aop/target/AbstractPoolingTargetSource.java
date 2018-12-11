package org.springframework.aop.target;

import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;

//抽象池化目标源
@SuppressWarnings("serial")
public abstract class AbstractPoolingTargetSource extends AbstractPrototypeBasedTargetSource
		implements PoolingConfig, DisposableBean {

	/** The maximum size of the pool */
	private int maxSize = -1;

	//设置最大大小
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	//获取最大大小
	@Override
	public int getMaxSize() {
		return this.maxSize;
	}

	//设置Bean工厂
	@Override
	public final void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		super.setBeanFactory(beanFactory);
		try {
			createPool();
		}
		catch (Throwable ex) {
			throw new BeanInitializationException("Could not create instance pool for TargetSource", ex);
		}
	}

	//创建池
	protected abstract void createPool() throws Exception;

	//获取目标
	@Override
	public abstract Object getTarget() throws Exception;

	//释放目标
	@Override
	public abstract void releaseTarget(Object target) throws Exception;


	/**
	 * Return an IntroductionAdvisor that providing a mixin
	 * exposing statistics about the pool maintained by this object.
	 */
	public DefaultIntroductionAdvisor getPoolingConfigMixin() {
		DelegatingIntroductionInterceptor dii = new DelegatingIntroductionInterceptor(this);
		return new DefaultIntroductionAdvisor(dii, PoolingConfig.class);
	}

}
