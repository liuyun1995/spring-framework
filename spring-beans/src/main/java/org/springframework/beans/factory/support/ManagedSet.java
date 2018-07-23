package org.springframework.beans.factory.support;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;

//管理的Set
@SuppressWarnings("serial")
public class ManagedSet<E> extends LinkedHashSet<E> implements Mergeable, BeanMetadataElement {

	private Object source;

	private String elementTypeName;

	private boolean mergeEnabled;


	public ManagedSet() {}

	public ManagedSet(int initialCapacity) {
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
	public Set<E> merge(Object parent) {
		if (!this.mergeEnabled) {
			throw new IllegalStateException("Not allowed to merge when the 'mergeEnabled' property is set to 'false'");
		}
		if (parent == null) {
			return this;
		}
		if (!(parent instanceof Set)) {
			throw new IllegalArgumentException("Cannot merge with object of type [" + parent.getClass() + "]");
		}
		Set<E> merged = new ManagedSet<E>();
		merged.addAll((Set<E>) parent);
		merged.addAll(this);
		return merged;
	}

}
