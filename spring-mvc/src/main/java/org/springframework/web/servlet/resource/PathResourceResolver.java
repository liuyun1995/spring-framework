package org.springframework.web.servlet.resource;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.ServletContextResource;

//路径资源解析器
public class PathResourceResolver extends AbstractResourceResolver {

	private Resource[] allowedLocations;

	//设置允许的资源路径
	public void setAllowedLocations(Resource... locations) {
		this.allowedLocations = locations;
	}

	//获取允许的资源路径
	public Resource[] getAllowedLocations() {
		return this.allowedLocations;
	}

	//解析资源
	@Override
	protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath,
			List<? extends Resource> locations, ResourceResolverChain chain) {

		return getResource(requestPath, locations);
	}

	//解析URL
	@Override
	protected String resolveUrlPathInternal(String resourcePath, List<? extends Resource> locations,
			ResourceResolverChain chain) {

		return (StringUtils.hasText(resourcePath) && getResource(resourcePath, locations) != null ? resourcePath : null);
	}

	//获取资源
	private Resource getResource(String resourcePath, List<? extends Resource> locations) {
		for (Resource location : locations) {
			try {
				if (logger.isTraceEnabled()) {
					logger.trace("Checking location: " + location);
				}
				Resource resource = getResource(resourcePath, location);
				if (resource != null) {
					if (logger.isTraceEnabled()) {
						logger.trace("Found match: " + resource);
					}
					return resource;
				}
				else if (logger.isTraceEnabled()) {
					logger.trace("No match for location: " + location);
				}
			}
			catch (IOException ex) {
				logger.trace("Failure checking for relative resource - trying next location", ex);
			}
		}
		return null;
	}

	//获取资源
	protected Resource getResource(String resourcePath, Resource location) throws IOException {
		Resource resource = location.createRelative(resourcePath);
		if (resource.exists() && resource.isReadable()) {
			if (checkResource(resource, location)) {
				return resource;
			}
			else if (logger.isTraceEnabled()) {
				logger.trace("Resource path=\"" + resourcePath + "\" was successfully resolved " +
						"but resource=\"" +	resource.getURL() + "\" is neither under the " +
						"current location=\"" + location.getURL() + "\" nor under any of the " +
						"allowed locations=" + Arrays.asList(getAllowedLocations()));
			}
		}
		return null;
	}

	//检验资源
	protected boolean checkResource(Resource resource, Resource location) throws IOException {
		if (isResourceUnderLocation(resource, location)) {
			return true;
		}
		if (getAllowedLocations() != null) {
			for (Resource current : getAllowedLocations()) {
				if (isResourceUnderLocation(resource, current)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isResourceUnderLocation(Resource resource, Resource location) throws IOException {
		if (resource.getClass() != location.getClass()) {
			return false;
		}

		String resourcePath;
		String locationPath;

		if (resource instanceof UrlResource) {
			resourcePath = resource.getURL().toExternalForm();
			locationPath = StringUtils.cleanPath(location.getURL().toString());
		}
		else if (resource instanceof ClassPathResource) {
			resourcePath = ((ClassPathResource) resource).getPath();
			locationPath = StringUtils.cleanPath(((ClassPathResource) location).getPath());
		}
		else if (resource instanceof ServletContextResource) {
			resourcePath = ((ServletContextResource) resource).getPath();
			locationPath = StringUtils.cleanPath(((ServletContextResource) location).getPath());
		}
		else {
			resourcePath = resource.getURL().getPath();
			locationPath = StringUtils.cleanPath(location.getURL().getPath());
		}

		if (locationPath.equals(resourcePath)) {
			return true;
		}
		locationPath = (locationPath.endsWith("/") || locationPath.isEmpty() ? locationPath : locationPath + "/");
		if (!resourcePath.startsWith(locationPath)) {
			return false;
		}

		if (resourcePath.contains("%")) {
			// Use URLDecoder (vs UriUtils) to preserve potentially decoded UTF-8 chars...
			if (URLDecoder.decode(resourcePath, "UTF-8").contains("../")) {
				if (logger.isTraceEnabled()) {
					logger.trace("Resolved resource path contains \"../\" after decoding: " + resourcePath);
				}
				return false;
			}
		}

		return true;
	}

}
