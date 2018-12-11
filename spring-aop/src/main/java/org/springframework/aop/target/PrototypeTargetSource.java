package org.springframework.aop.target;

import org.springframework.beans.BeansException;

//原型目标源
@SuppressWarnings("serial")
public class PrototypeTargetSource extends AbstractPrototypeBasedTargetSource {

	//获取目标
	@Override
	public Object getTarget() throws BeansException {
		return newPrototypeInstance();
	}

	//释放目标
	@Override
	public void releaseTarget(Object target) {
		destroyPrototypeInstance(target);
	}

	@Override
	public String toString() {
		return "PrototypeTargetSource for target bean with name '" + getTargetBeanName() + "'";
	}

}
