package org.springframework.beans.factory.xml;

//名称空间处理器转换器
public interface NamespaceHandlerResolver {

	//根据uri获取名称空间处理器
	NamespaceHandler resolve(String namespaceUri);

}
