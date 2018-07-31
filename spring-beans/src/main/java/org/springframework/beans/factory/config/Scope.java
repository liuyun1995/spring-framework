package org.springframework.beans.factory.config;

import org.springframework.beans.factory.ObjectFactory;

public interface Scope {

	//获取对象
	Object get(String name, ObjectFactory<?> objectFactory);

	//移除对象
	Object remove(String name);

	void registerDestructionCallback(String name, Runnable callback);

	Object resolveContextualObject(String key);

	String getConversationId();

}
