package org.springframework.web.servlet.mvc.annotation;

import java.lang.reflect.Method;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.ModelAndView;

//模型视图解析器
public interface ModelAndViewResolver {

	/**
	 * Marker to be returned when the resolver does not know how to handle the given method parameter.
	 */
	ModelAndView UNRESOLVED = new ModelAndView();


	ModelAndView resolveModelAndView(Method handlerMethod, Class<?> handlerType, Object returnValue,
			ExtendedModelMap implicitModel, NativeWebRequest webRequest);

}
