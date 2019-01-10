package org.springframework.web.servlet.handler;

import java.util.Map;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;

//请求匹配结果
public class RequestMatchResult {

	private final String matchingPattern;
	private final String lookupPath;
	private final PathMatcher pathMatcher;

	//构造器
	public RequestMatchResult(String matchingPattern, String lookupPath, PathMatcher pathMatcher) {
		Assert.hasText(matchingPattern, "'matchingPattern' is required");
		Assert.hasText(lookupPath, "'lookupPath' is required");
		Assert.notNull(pathMatcher, "'pathMatcher' is required");
		this.matchingPattern = matchingPattern;
		this.lookupPath = lookupPath;
		this.pathMatcher = pathMatcher;
	}

	//额外的URI临时变量
	public Map<String, String> extractUriTemplateVariables() {
		return this.pathMatcher.extractUriTemplateVariables(this.matchingPattern, this.lookupPath);
	}

}
