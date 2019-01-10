package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;

//最后修改时间
public interface LastModified {

	//获取最后修改时间戳
	long getLastModified(HttpServletRequest request);

}
