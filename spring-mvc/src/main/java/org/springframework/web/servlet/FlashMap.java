package org.springframework.web.servlet;

import java.util.HashMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * A FlashMap provides a way for one request to store attributes intended for
 * use in another. This is most commonly needed when redirecting from one URL
 * to another -- e.g. the Post/Redirect/Get pattern. A FlashMap is saved before
 * the redirect (typically in the session) and is made available after the
 * redirect and removed immediately.
 *
 * <p>A FlashMap can be set up with a request path and request parameters to
 * help identify the target request. Without this information, a FlashMap is
 * made available to the next request, which may or may not be the intended
 * recipient. On a redirect, the target URL is known and a FlashMap can be
 * updated with that information. This is done automatically when the
 * {@code org.springframework.web.servlet.view.RedirectView} is used.
 *
 * <p>Note: annotated controllers will usually not use FlashMap directly.
 * See {@code org.springframework.web.servlet.mvc.support.RedirectAttributes}
 * for an overview of using flash attributes in annotated controllers.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 * @see FlashMapManager
 */
@SuppressWarnings("serial")
public final class FlashMap extends HashMap<String, Object> implements Comparable<FlashMap> {

	private String targetRequestPath;

	private final MultiValueMap<String, String> targetRequestParams = new LinkedMultiValueMap<String, String>(4);

	private long expirationTime = -1;

	//设置目标请求路径
	public void setTargetRequestPath(String path) {
		this.targetRequestPath = path;
	}

	//获取目标请求路径
	public String getTargetRequestPath() {
		return this.targetRequestPath;
	}

	//添加目标请求参数
	public FlashMap addTargetRequestParams(MultiValueMap<String, String> params) {
		if (params != null) {
			for (String key : params.keySet()) {
				for (String value : params.get(key)) {
					addTargetRequestParam(key, value);
				}
			}
		}
		return this;
	}

	//添加目标请求参数
	public FlashMap addTargetRequestParam(String name, String value) {
		if (StringUtils.hasText(name) && StringUtils.hasText(value)) {
			this.targetRequestParams.add(name, value);
		}
		return this;
	}

	//获取目标请求参数
	public MultiValueMap<String, String> getTargetRequestParams() {
		return this.targetRequestParams;
	}

	//设置存活时间
	public void startExpirationPeriod(int timeToLive) {
		this.expirationTime = System.currentTimeMillis() + timeToLive * 1000;
	}

	//设置过期时间
	public void setExpirationTime(long expirationTime) {
		this.expirationTime = expirationTime;
	}

	//获取过期时间
	public long getExpirationTime() {
		return this.expirationTime;
	}

	//是否已经过期
	public boolean isExpired() {
		return (this.expirationTime != -1 && System.currentTimeMillis() > this.expirationTime);
	}

	@Override
	public int compareTo(FlashMap other) {
		int thisUrlPath = (this.targetRequestPath != null ? 1 : 0);
		int otherUrlPath = (other.targetRequestPath != null ? 1 : 0);
		if (thisUrlPath != otherUrlPath) {
			return otherUrlPath - thisUrlPath;
		}
		else {
			return other.targetRequestParams.size() - this.targetRequestParams.size();
		}
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof FlashMap)) {
			return false;
		}
		FlashMap otherFlashMap = (FlashMap) other;
		return (super.equals(otherFlashMap) &&
				ObjectUtils.nullSafeEquals(this.targetRequestPath, otherFlashMap.targetRequestPath) &&
				this.targetRequestParams.equals(otherFlashMap.targetRequestParams));
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + ObjectUtils.nullSafeHashCode(this.targetRequestPath);
		result = 31 * result + this.targetRequestParams.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "FlashMap [attributes=" + super.toString() + ", targetRequestPath=" +
				this.targetRequestPath + ", targetRequestParams=" + this.targetRequestParams + "]";
	}

}
