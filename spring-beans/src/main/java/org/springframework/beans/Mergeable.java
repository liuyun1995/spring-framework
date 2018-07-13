package org.springframework.beans;

public interface Mergeable {
	
	boolean isMergeEnabled();
	
	Object merge(Object parent);

}
