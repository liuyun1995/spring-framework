package org.springframework.web.servlet;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;

//视图接口
public interface View {

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the response status code.
	 * <p>Note: This attribute is not required to be supported by all View implementations.
	 */
	String RESPONSE_STATUS_ATTRIBUTE = View.class.getName() + ".responseStatus";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains a Map with path variables.
	 * The map consists of String-based URI template variable names as keys and their corresponding
	 * Object-based values -- extracted from segments of the URL and type converted.
	 *
	 * <p>Note: This attribute is not required to be supported by all View implementations.
	 */
	String PATH_VARIABLES = View.class.getName() + ".pathVariables";

	/**
	 * The {@link MediaType} selected during content negotiation, which may be
	 * more specific than the one the View is configured with. For example:
	 * "application/vnd.example-v1+xml" vs "application/*+xml".
	 */
	String SELECTED_CONTENT_TYPE = View.class.getName() + ".selectedContentType";

	/**
	 * Return the content type of the view, if predetermined.
	 * <p>Can be used to check the content type upfront,
	 * before the actual rendering process.
	 * @return the content type String (optionally including a character set),
	 * or {@code null} if not predetermined.
	 */
	String getContentType();

	//渲染视图
	void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
