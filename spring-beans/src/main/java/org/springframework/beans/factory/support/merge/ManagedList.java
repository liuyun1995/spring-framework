package org.springframework.beans.factory.support.merge;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.bean.BeanMetadataElement;

@SuppressWarnings("serial")
public class ManagedList<E> extends ArrayList<E> implements Mergeable, BeanMetadataElement {

	private Object source;
	private String elementTypeName;
	private boolean mergeEnabled;

	public ManagedList() {}

	public ManagedList(int initialCapacity) {
		super(initialCapacity);
	}
	
	public void setSource(Object source) {
		this.source = source;
	}

	@Override
	public Object getSource() {
		return this.source;
	}
	
	public void setElementTypeName(String elementTypeName) {
		this.elementTypeName = elementTypeName;
	}
	
	public String getElementTypeName() {
		return this.elementTypeName;
	}
	
	public void setMergeEnabled(boolean mergeEnabled) {
		this.mergeEnabled = mergeEnabled;
	}

	@Override
	public boolean isMergeEnabled() {
		return this.mergeEnabled;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<E> merge(Object parent) {
		if (!this.mergeEnabled) {
			throw new IllegalStateException("Not allowed to merge when the 'mergeEnabled' property is set to 'false'");
		}
		if (parent == null) {
			return this;
		}
		if (!(parent instanceof List)) {
			throw new IllegalArgumentException("Cannot merge with object of type [" + parent.getClass() + "]");
		}
		List<E> merged = new ManagedList<E>();
		merged.addAll((List<E>) parent);
		merged.addAll(this);
		return merged;
	}

}
