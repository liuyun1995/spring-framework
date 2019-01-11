package org.springframework.util;

//错误处理接口
public interface ErrorHandler {

	//处理错误
	void handleError(Throwable t);

}
