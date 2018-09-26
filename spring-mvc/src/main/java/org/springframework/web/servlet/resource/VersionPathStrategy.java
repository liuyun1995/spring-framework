package org.springframework.web.servlet.resource;

//版本路径策略
public interface VersionPathStrategy {

	//获取版本
	String extractVersion(String requestPath);

	//移除版本
	String removeVersion(String requestPath, String version);

	//添加版本
	String addVersion(String requestPath, String version);

}
