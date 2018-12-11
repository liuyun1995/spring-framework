package org.springframework.aop.target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;

//抽象懒创建目标源
public abstract class AbstractLazyCreationTargetSource implements TargetSource {

	protected final Log logger = LogFactory.getLog(getClass());

	private Object lazyTarget;   //懒加载目标对象

	//是否初始化过
	public synchronized boolean isInitialized() {
		return (this.lazyTarget != null);
	}

	//获取目标类型
	@Override
	public synchronized Class<?> getTargetClass() {
		return (this.lazyTarget != null ? this.lazyTarget.getClass() : null);
	}

	//是否是静态的
	@Override
	public boolean isStatic() {
		return false;
	}

	//获取目标对象
	@Override
	public synchronized Object getTarget() throws Exception {
		if (this.lazyTarget == null) {
			logger.debug("Initializing lazy target object");
			this.lazyTarget = createObject();
		}
		return this.lazyTarget;
	}

	//释放目标对象
	@Override
	public void releaseTarget(Object target) throws Exception {
		// nothing to do
	}

	//创建对象
	protected abstract Object createObject() throws Exception;

}
