package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.UrlPathHelper;

/**
 * Abstract base class for {@code Controllers} that return a view name
 * based on the request URL.
 *
 * <p>Provides infrastructure for determining view names from URLs and configurable
 * URL lookup. For information on the latter, see {@code alwaysUseFullPath}
 * and {@code urlDecode} properties.
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 * @see #setAlwaysUseFullPath
 * @see #setUrlDecode
 */
public abstract class AbstractUrlViewController extends AbstractController {

	private UrlPathHelper urlPathHelper = new UrlPathHelper();

	//设置总是使用全路径
	public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
		this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
	}

	//设置是否对URL解码
	public void setUrlDecode(boolean urlDecode) {
		this.urlPathHelper.setUrlDecode(urlDecode);
	}

	/**
	 * Set if ";" (semicolon) content should be stripped from the request URI.
	 * @see org.springframework.web.util.UrlPathHelper#setRemoveSemicolonContent(boolean)
	 */
	public void setRemoveSemicolonContent(boolean removeSemicolonContent) {
		this.urlPathHelper.setRemoveSemicolonContent(removeSemicolonContent);
	}

	//设置URL路径助手
	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
		this.urlPathHelper = urlPathHelper;
	}

	//获取URL路径助手
	protected UrlPathHelper getUrlPathHelper() {
		return this.urlPathHelper;
	}

	//核心处理请求方法
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
		String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
		String viewName = getViewNameForRequest(request);
		if (logger.isDebugEnabled()) {
			logger.debug("Returning view name '" + viewName + "' for lookup path [" + lookupPath + "]");
		}
		return new ModelAndView(viewName, RequestContextUtils.getInputFlashMap(request));
	}

	//根据请求获取视图名
	protected abstract String getViewNameForRequest(HttpServletRequest request);

}
