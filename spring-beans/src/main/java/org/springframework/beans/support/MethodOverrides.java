package org.springframework.beans.support;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

//方法覆盖
public class MethodOverrides {

	private final Set<org.springframework.beans.support.MethodOverride> overrides = Collections.synchronizedSet(new LinkedHashSet<org.springframework.beans.support.MethodOverride>(0));
	private volatile boolean modified = false;

	//构造器1
	public MethodOverrides() {}

	//构造器2
	public MethodOverrides(MethodOverrides other) {
		addOverrides(other);
	}

	//添加覆盖者集合
	public void addOverrides(MethodOverrides other) {
		if (other != null) {
			this.modified = true;
			this.overrides.addAll(other.overrides);
		}
	}

	//添加覆盖着
	public void addOverride(org.springframework.beans.support.MethodOverride override) {
		this.modified = true;
		this.overrides.add(override);
	}

	//获取所有覆盖者
	public Set<org.springframework.beans.support.MethodOverride> getOverrides() {
		this.modified = true;
		return this.overrides;
	}

	//覆盖着是否为空
	public boolean isEmpty() {
		return (!this.modified || this.overrides.isEmpty());
	}

	//获取指定方法覆盖者
	public org.springframework.beans.support.MethodOverride getOverride(Method method) {
		if (!this.modified) {
			return null;
		}
		synchronized (this.overrides) {
			org.springframework.beans.support.MethodOverride match = null;
			for (MethodOverride candidate : this.overrides) {
				if (candidate.matches(method)) {
					match = candidate;
				}
			}
			return match;
		}
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MethodOverrides)) {
			return false;
		}
		MethodOverrides that = (MethodOverrides) other;
		return this.overrides.equals(that.overrides);

	}

	@Override
	public int hashCode() {
		return this.overrides.hashCode();
	}

}
