package org.springframework.web.servlet.mvc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;

//URL文件名视图控制器
public class UrlFilenameViewController extends AbstractUrlViewController {

	private String prefix = "";

	private String suffix = "";

	private final Map<String, String> viewNameCache = new ConcurrentHashMap<String, String>(256);

	//设置前缀
	public void setPrefix(String prefix) {
		this.prefix = (prefix != null ? prefix : "");
	}

	//获取前缀
	protected String getPrefix() {
		return this.prefix;
	}

	//设置后缀
	public void setSuffix(String suffix) {
		this.suffix = (suffix != null ? suffix : "");
	}

	//获取后缀
	protected String getSuffix() {
		return this.suffix;
	}

	//获取视图名称
	@Override
	protected String getViewNameForRequest(HttpServletRequest request) {
		String uri = extractOperableUrl(request);
		return getViewNameForUrlPath(uri);
	}

	/**
	 * Extract a URL path from the given request,
	 * suitable for view name extraction.
	 * @param request current HTTP request
	 * @return the URL to use for view name extraction
	 */
	protected String extractOperableUrl(HttpServletRequest request) {
		String urlPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		if (!StringUtils.hasText(urlPath)) {
			urlPath = getUrlPathHelper().getLookupPathForRequest(request);
		}
		return urlPath;
	}

	//通过URL路径获取视图名称
	protected String getViewNameForUrlPath(String uri) {
		String viewName = this.viewNameCache.get(uri);
		if (viewName == null) {
			viewName = extractViewNameFromUrlPath(uri);
			viewName = postProcessViewName(viewName);
			this.viewNameCache.put(uri, viewName);
		}
		return viewName;
	}

	/**
	 * Extract the URL filename from the given request URI.
	 * @param uri the request URI; for example {@code "/index.html"}
	 * @return the extracted URI filename; for example {@code "index"}
	 */
	protected String extractViewNameFromUrlPath(String uri) {
		int start = (uri.charAt(0) == '/' ? 1 : 0);
		int lastIndex = uri.lastIndexOf(".");
		int end = (lastIndex < 0 ? uri.length() : lastIndex);
		return uri.substring(start, end);
	}

	//加工视图名称
	protected String postProcessViewName(String viewName) {
		return getPrefix() + viewName + getSuffix();
	}

}
