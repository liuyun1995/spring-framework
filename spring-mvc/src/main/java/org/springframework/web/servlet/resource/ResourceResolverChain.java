package org.springframework.web.servlet.resource;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;

//资源解析器链
public interface ResourceResolverChain {

	//解析资源
	Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations);

	//解析URL
	String resolveUrlPath(String resourcePath, List<? extends Resource> locations);

}
