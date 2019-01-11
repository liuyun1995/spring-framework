package org.springframework.web.servlet.config.annotation;

import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

//WebMvc配置器
public interface WebMvcConfigurer {

	//配置路径匹配器
	void configurePathMatch(PathMatchConfigurer configurer);

	/**
	 * Configure content negotiation options.
	 */
	void configureContentNegotiation(ContentNegotiationConfigurer configurer);

	//配置异步支持
	void configureAsyncSupport(AsyncSupportConfigurer configurer);

	//配置默认Servlet处理器
	void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer);

	//添加格式化器
	void addFormatters(FormatterRegistry registry);

	//添加拦截器
	void addInterceptors(InterceptorRegistry registry);

	//添加资源处理器
	void addResourceHandlers(ResourceHandlerRegistry registry);

	//配置跨域请求处理
	void addCorsMappings(CorsRegistry registry);

	//添加视图控制器
	void addViewControllers(ViewControllerRegistry registry);

	//配置视图解析器
	void configureViewResolvers(ViewResolverRegistry registry);

	//添加参数解析器
	void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers);

	//添加返回结果处理器
	void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers);

	//配置消息转换器
	void configureMessageConverters(List<HttpMessageConverter<?>> converters);

	//拓展消息转换器
	void extendMessageConverters(List<HttpMessageConverter<?>> converters);

	//配置处理器异常解析器
	void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers);

	//拓展处理器异常解析器
	void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers);

	//获取验证器
	Validator getValidator();

	//获取消息码解析器
	MessageCodesResolver getMessageCodesResolver();

}
