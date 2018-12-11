package org.springframework.aop.target;

import org.springframework.beans.BeansException;

//懒加载目标源
@SuppressWarnings("serial")
public class LazyInitTargetSource extends AbstractBeanFactoryBasedTargetSource {

	private Object target;

	@Override
	public synchronized Object getTarget() throws BeansException {
		if (this.target == null) {
			this.target = getBeanFactory().getBean(getTargetBeanName());
			postProcessTargetObject(this.target);
		}
		return this.target;
	}

	/**
	 * Subclasses may override this method to perform additional processing on
	 * the target object when it is first loaded.
	 * @param targetObject the target object that has just been instantiated (and configured)
	 */
	protected void postProcessTargetObject(Object targetObject) {
	}

}
