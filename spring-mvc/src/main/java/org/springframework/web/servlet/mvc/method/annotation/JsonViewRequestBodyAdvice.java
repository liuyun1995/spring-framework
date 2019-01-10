package org.springframework.web.servlet.mvc.method.annotation;

import java.io.IOException;
import java.lang.reflect.Type;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;

//@RequestBody切面(JSON转实体)
public class JsonViewRequestBodyAdvice extends RequestBodyAdviceAdapter {

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {

		return (AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType) &&
				methodParameter.getParameterAnnotation(JsonView.class) != null);
	}

	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter methodParameter,
			Type targetType, Class<? extends HttpMessageConverter<?>> selectedConverterType) throws IOException {

		JsonView annotation = methodParameter.getParameterAnnotation(JsonView.class);
		Class<?>[] classes = annotation.value();
		if (classes.length != 1) {
			throw new IllegalArgumentException(
					"@JsonView only supported for request body advice with exactly 1 class argument: " + methodParameter);
		}
		return new MappingJacksonInputMessage(inputMessage.getBody(), inputMessage.getHeaders(), classes[0]);
	}

}
