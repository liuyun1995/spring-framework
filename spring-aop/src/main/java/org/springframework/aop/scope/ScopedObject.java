package org.springframework.aop.scope;

import org.springframework.aop.RawTargetAccess;

//范围对象
public interface ScopedObject extends RawTargetAccess {

	Object getTargetObject();

	void removeFromScope();

}
