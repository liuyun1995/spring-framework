package org.springframework.beans.support.merge;

import java.util.Properties;
import org.springframework.beans.bean.BeanMetadataElement;

@SuppressWarnings("serial")
public class ManagedProperties extends Properties implements Mergeable, BeanMetadataElement {

	private Object source;
	private boolean mergeEnabled;

	public void setSource(Object source) {
		this.source = source;
	}

	@Override
	public Object getSource() {
		return this.source;
	}

	public void setMergeEnabled(boolean mergeEnabled) {
		this.mergeEnabled = mergeEnabled;
	}

	@Override
	public boolean isMergeEnabled() {
		return this.mergeEnabled;
	}

	@Override
	public Object merge(Object parent) {
		if (!this.mergeEnabled) {
			throw new IllegalStateException("Not allowed to merge when the 'mergeEnabled' property is set to 'false'");
		}
		if (parent == null) {
			return this;
		}
		if (!(parent instanceof Properties)) {
			throw new IllegalArgumentException("Cannot merge with object of type [" + parent.getClass() + "]");
		}
		Properties merged = new ManagedProperties();
		merged.putAll((Properties) parent);
		merged.putAll(this);
		return merged;
	}

}
