package org.springframework.util;

import java.util.Collection;
import java.util.Collections;

//实例过滤器
public class InstanceFilter<T> {

	private final Collection<? extends T> includes;

	private final Collection<? extends T> excludes;

	private final boolean matchIfEmpty;

	//构造器
	public InstanceFilter(Collection<? extends T> includes,
			Collection<? extends T> excludes, boolean matchIfEmpty) {

		this.includes = (includes != null ? includes : Collections.<T>emptyList());
		this.excludes = (excludes != null ? excludes : Collections.<T>emptyList());
		this.matchIfEmpty = matchIfEmpty;
	}

	//匹配方法
	public boolean match(T instance) {
		Assert.notNull(instance, "Instance to match must not be null");

		boolean includesSet = !this.includes.isEmpty();
		boolean excludesSet = !this.excludes.isEmpty();
		if (!includesSet && !excludesSet) {
			return this.matchIfEmpty;
		}

		boolean matchIncludes = match(instance, this.includes);
		boolean matchExcludes = match(instance, this.excludes);
		if (!includesSet) {
			return !matchExcludes;
		}
		if (!excludesSet) {
			return matchIncludes;
		}
		return matchIncludes && !matchExcludes;
	}

	//匹配方法
	protected boolean match(T instance, T candidate) {
		return instance.equals(candidate);
	}

	//匹配方法
	protected boolean match(T instance, Collection<? extends T> candidates) {
		for (T candidate : candidates) {
			if (match(instance, candidate)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append(": includes=").append(this.includes);
		sb.append(", excludes=").append(this.excludes);
		sb.append(", matchIfEmpty=").append(this.matchIfEmpty);
		return sb.toString();
	}

}
