package org.springframework.beans.factory.wiring;

public interface BeanWiringInfoResolver {

	BeanWiringInfo resolveWiringInfo(Object beanInstance);

}
