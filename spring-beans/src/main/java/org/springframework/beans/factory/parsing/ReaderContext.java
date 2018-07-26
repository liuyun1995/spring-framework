package org.springframework.beans.factory.parsing;

import org.springframework.core.io.Resource;

//解析上下文
public class ReaderContext {

	private final Resource resource;                      //当前XML资源
	private final ProblemReporter problemReporter;        //问题报告器
	private final ReaderEventListener eventListener;      //解析事件监听器
	private final SourceExtractor sourceExtractor;        //额外XMl资源

	//构造器
	public ReaderContext(Resource resource, ProblemReporter problemReporter,
			ReaderEventListener eventListener, SourceExtractor sourceExtractor) {
		this.resource = resource;
		this.problemReporter = problemReporter;
		this.eventListener = eventListener;
		this.sourceExtractor = sourceExtractor;
	}

	//获取当前资源
	public final Resource getResource() {
		return this.resource;
	}

	//记录致命错误
	public void fatal(String message, Object source) {
		fatal(message, source, null, null);
	}

	//记录致命错误
	public void fatal(String message, Object source, Throwable ex) {
		fatal(message, source, null, ex);
	}

	//记录致命错误
	public void fatal(String message, Object source, ParseState parseState) {
		fatal(message, source, parseState, null);
	}

	//记录致命错误
	public void fatal(String message, Object source, ParseState parseState, Throwable cause) {
		Location location = new Location(getResource(), source);
		this.problemReporter.fatal(new Problem(message, location, parseState, cause));
	}

	//记录错误消息
	public void error(String message, Object source) {
		error(message, source, null, null);
	}

	//记录错误消息
	public void error(String message, Object source, Throwable ex) {
		error(message, source, null, ex);
	}

	//记录错误消息
	public void error(String message, Object source, ParseState parseState) {
		error(message, source, parseState, null);
	}

	//记录错误消息
	public void error(String message, Object source, ParseState parseState, Throwable cause) {
		Location location = new Location(getResource(), source);
		this.problemReporter.error(new Problem(message, location, parseState, cause));
	}

	//记录警告消息
	public void warning(String message, Object source) {
		warning(message, source, null, null);
	}

	//记录警告消息
	public void warning(String message, Object source, Throwable ex) {
		warning(message, source, null, ex);
	}

	//记录警告消息
	public void warning(String message, Object source, ParseState parseState) {
		warning(message, source, parseState, null);
	}

	//记录警告消息
	public void warning(String message, Object source, ParseState parseState, Throwable cause) {
		Location location = new Location(getResource(), source);
		this.problemReporter.warning(new Problem(message, location, parseState, cause));
	}

	public void fireDefaultsRegistered(DefaultsDefinition defaultsDefinition) {
		this.eventListener.defaultsRegistered(defaultsDefinition);
	}

	public void fireComponentRegistered(ComponentDefinition componentDefinition) {
		this.eventListener.componentRegistered(componentDefinition);
	}

	public void fireAliasRegistered(String beanName, String alias, Object source) {
		this.eventListener.aliasRegistered(new AliasDefinition(beanName, alias, source));
	}

	public void fireImportProcessed(String importedResource, Object source) {
		this.eventListener.importProcessed(new ImportDefinition(importedResource, source));
	}

	public void fireImportProcessed(String importedResource, Resource[] actualResources, Object source) {
		this.eventListener.importProcessed(new ImportDefinition(importedResource, actualResources, source));
	}

	//设置额外资源
	public Object extractSource(Object sourceCandidate) {
		return this.sourceExtractor.extractSource(sourceCandidate, this.resource);
	}

	//获取额外资源
	public SourceExtractor getSourceExtractor() {
		return this.sourceExtractor;
	}

}
