package org.springframework.web.servlet.config.annotation;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.DeferredResultProcessingInterceptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helps with configuring options for asynchronous request processing.
 *
 * @author Rossen Stoyanchev
 * @since 3.2
 */
public class AsyncSupportConfigurer {

	private AsyncTaskExecutor taskExecutor;

	private Long timeout;

	private final List<CallableProcessingInterceptor> callableInterceptors = new ArrayList<CallableProcessingInterceptor>();

	private final List<DeferredResultProcessingInterceptor> deferredResultInterceptors = new ArrayList<DeferredResultProcessingInterceptor>();


	//设置任务执行器
	public AsyncSupportConfigurer setTaskExecutor(AsyncTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
		return this;
	}

	//设置默认超时时间
	public AsyncSupportConfigurer setDefaultTimeout(long timeout) {
		this.timeout = timeout;
		return this;
	}

	//注册可调用的拦截器
	public AsyncSupportConfigurer registerCallableInterceptors(CallableProcessingInterceptor... interceptors) {
		this.callableInterceptors.addAll(Arrays.asList(interceptors));
		return this;
	}

	//注册延迟结果拦截器
	public AsyncSupportConfigurer registerDeferredResultInterceptors(DeferredResultProcessingInterceptor... interceptors) {
		this.deferredResultInterceptors.addAll(Arrays.asList(interceptors));
		return this;
	}

	//获取任务执行器
	protected AsyncTaskExecutor getTaskExecutor() {
		return this.taskExecutor;
	}

	//获取超时时间
	protected Long getTimeout() {
		return this.timeout;
	}

	//获取可调用的拦截器
	protected List<CallableProcessingInterceptor> getCallableInterceptors() {
		return this.callableInterceptors;
	}

	//获取延迟结果拦截器
	protected List<DeferredResultProcessingInterceptor> getDeferredResultInterceptors() {
		return this.deferredResultInterceptors;
	}

}
