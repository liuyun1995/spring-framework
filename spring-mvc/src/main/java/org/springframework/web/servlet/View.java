package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

//视图接口
public interface View {

	//响应状态属性
	String RESPONSE_STATUS_ATTRIBUTE = View.class.getName() + ".responseStatus";

	//路径变量
	String PATH_VARIABLES = View.class.getName() + ".pathVariables";

	//选择的HTTP文本内容类型
	String SELECTED_CONTENT_TYPE = View.class.getName() + ".selectedContentType";

	//获取HTTP文本内容类型
	String getContentType();

	//渲染视图
	void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
