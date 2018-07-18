package org.springframework.aop.target.dynamic;

public interface Refreshable {

	//刷新
	void refresh();

	//获取刷新次数
	long getRefreshCount();

	//上一次刷新时间
	long getLastRefreshTime();

}
