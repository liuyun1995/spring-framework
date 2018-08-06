package org.springframework.beans.factory.support.merge;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.BeanMetadataElement;

//管理的映射
@SuppressWarnings("serial")
public class ManagedMap<K, V> extends LinkedHashMap<K, V> implements Mergeable, BeanMetadataElement {

	private Object source;

	private String keyTypeName;

	private String valueTypeName;

	private boolean mergeEnabled;


	public ManagedMap() {}

	public ManagedMap(int initialCapacity) {
		super(initialCapacity);
	}

	public void setSource(Object source) {
		this.source = source;
	}

	@Override
	public Object getSource() {
		return this.source;
	}

	/**
	 * Set the default key type name (class name) to be used for this map.
	 */
	public void setKeyTypeName(String keyTypeName) {
		this.keyTypeName = keyTypeName;
	}

	/**
	 * Return the default key type name (class name) to be used for this map.
	 */
	public String getKeyTypeName() {
		return this.keyTypeName;
	}

	/**
	 * Set the default value type name (class name) to be used for this map.
	 */
	public void setValueTypeName(String valueTypeName) {
		this.valueTypeName = valueTypeName;
	}

	/**
	 * Return the default value type name (class name) to be used for this map.
	 */
	public String getValueTypeName() {
		return this.valueTypeName;
	}

	/**
	 * Set whether merging should be enabled for this collection,
	 * in case of a 'parent' collection value being present.
	 */
	public void setMergeEnabled(boolean mergeEnabled) {
		this.mergeEnabled = mergeEnabled;
	}

	@Override
	public boolean isMergeEnabled() {
		return this.mergeEnabled;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object merge(Object parent) {
		if (!this.mergeEnabled) {
			throw new IllegalStateException("Not allowed to merge when the 'mergeEnabled' property is set to 'false'");
		}
		if (parent == null) {
			return this;
		}
		if (!(parent instanceof Map)) {
			throw new IllegalArgumentException("Cannot merge with object of type [" + parent.getClass() + "]");
		}
		Map<K, V> merged = new ManagedMap<K, V>();
		merged.putAll((Map<K, V>) parent);
		merged.putAll(this);
		return merged;
	}

}
