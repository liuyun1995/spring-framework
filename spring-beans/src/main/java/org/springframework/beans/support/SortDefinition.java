package org.springframework.beans.support;

//排序定义
public interface SortDefinition {

	//获取属性
	String getProperty();

	//是否忽略大小写
	boolean isIgnoreCase();

	//是否升序
	boolean isAscending();

}
