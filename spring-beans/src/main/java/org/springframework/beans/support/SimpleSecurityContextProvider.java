package org.springframework.beans.support;

import java.security.AccessControlContext;
import java.security.AccessController;

//简单安全控制上下文提供者
public class SimpleSecurityContextProvider implements SecurityContextProvider {

	//访问控制上下文
	private final AccessControlContext acc;

	//构造器
	public SimpleSecurityContextProvider() {
		this(null);
	}

	//构造器
	public SimpleSecurityContextProvider(AccessControlContext acc) {
		this.acc = acc;
	}

	//获取访问控制上下文
	@Override
	public AccessControlContext getAccessControlContext() {
		return (this.acc != null ? acc : AccessController.getContext());
	}

}
