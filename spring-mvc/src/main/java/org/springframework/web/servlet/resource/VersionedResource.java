package org.springframework.web.servlet.resource;

import org.springframework.core.io.Resource;

//版本资源
public interface VersionedResource extends Resource {

	//获取版本
	String getVersion();

}
