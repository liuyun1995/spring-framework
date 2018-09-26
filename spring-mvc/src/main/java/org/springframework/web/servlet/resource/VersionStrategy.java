package org.springframework.web.servlet.resource;

import org.springframework.core.io.Resource;

//版本策略接口
public interface VersionStrategy extends VersionPathStrategy {

	//获取资源版本
	String getResourceVersion(Resource resource);

}
