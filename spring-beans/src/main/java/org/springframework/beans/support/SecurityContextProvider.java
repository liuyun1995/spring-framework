package org.springframework.beans.support;

import java.security.AccessControlContext;

//安全上下文提供者
public interface SecurityContextProvider {

	//获取访问控制上下文
	AccessControlContext getAccessControlContext();

}
