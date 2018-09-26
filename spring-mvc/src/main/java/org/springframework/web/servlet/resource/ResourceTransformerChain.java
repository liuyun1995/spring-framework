package org.springframework.web.servlet.resource;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;

//资源转换器链
public interface ResourceTransformerChain {

	//获取解析器链
	ResourceResolverChain getResolverChain();

	//转换资源
	Resource transform(HttpServletRequest request, Resource resource) throws IOException;

}
