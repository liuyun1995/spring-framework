package org.springframework.web.servlet.tags;

import javax.servlet.jsp.JspTagException;

//参数装配器
public interface ArgumentAware {

	//添加参数
	void addArgument(Object argument) throws JspTagException;

}
