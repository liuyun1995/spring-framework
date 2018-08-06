package org.springframework.beans.support.merge;

//可合并的
public interface Mergeable {

	//是否可合并
	boolean isMergeEnabled();

	//合并对象
	Object merge(Object parent);

}
