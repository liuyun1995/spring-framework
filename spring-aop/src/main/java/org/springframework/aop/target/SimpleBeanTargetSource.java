package org.springframework.aop.target;

//简单Bean目标源
@SuppressWarnings("serial")
public class SimpleBeanTargetSource extends AbstractBeanFactoryBasedTargetSource {

	@Override
	public Object getTarget() throws Exception {
		return getBeanFactory().getBean(getTargetBeanName());
	}

}
