package org.springframework.beans.factory.xml;

public interface NamespaceHandlerResolver {

	NamespaceHandler resolve(String namespaceUri);

}
