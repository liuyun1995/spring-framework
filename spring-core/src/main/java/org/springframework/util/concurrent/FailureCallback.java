package org.springframework.util.concurrent;

//失败回调接口
public interface FailureCallback {

	//失败进行回调
	void onFailure(Throwable ex);

}
