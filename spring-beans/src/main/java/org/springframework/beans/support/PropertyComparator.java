package org.springframework.beans.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.bean.BeanWrapperImpl;
import org.springframework.beans.exception.BeansException;
import org.springframework.util.StringUtils;

//属性比较器
public class PropertyComparator<T> implements Comparator<T> {

	protected final Log logger = LogFactory.getLog(getClass());              //日志类
	private final SortDefinition sortDefinition;                             //排序定义
	private final BeanWrapperImpl beanWrapper = new BeanWrapperImpl(false);  //Bean包装类

	//构造器1
	public PropertyComparator(SortDefinition sortDefinition) {
		this.sortDefinition = sortDefinition;
	}

	//构造器2
	public PropertyComparator(String property, boolean ignoreCase, boolean ascending) {
		this.sortDefinition = new MutableSortDefinition(property, ignoreCase, ascending);
	}

	//获取排序定义
	public final SortDefinition getSortDefinition() {
		return this.sortDefinition;
	}

	//比较方法
	@Override
	@SuppressWarnings("unchecked")
	public int compare(T o1, T o2) {
		Object v1 = getPropertyValue(o1);
		Object v2 = getPropertyValue(o2);
		if (this.sortDefinition.isIgnoreCase() && (v1 instanceof String) && (v2 instanceof String)) {
			v1 = ((String) v1).toLowerCase();
			v2 = ((String) v2).toLowerCase();
		}
		int result;
		try {
			if (v1 != null) {
				result = (v2 != null ? ((Comparable<Object>) v1).compareTo(v2) : -1);
			} else {
				result = (v2 != null ? 1 : 0);
			}
		} catch (RuntimeException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Could not sort objects [" + o1 + "] and [" + o2 + "]", ex);
			}
			return 0;
		}

		return (this.sortDefinition.isAscending() ? result : -result);
	}

	//获取属性值
	private Object getPropertyValue(Object obj) {
		try {
			this.beanWrapper.setWrappedInstance(obj);
			return this.beanWrapper.getPropertyValue(this.sortDefinition.getProperty());
		} catch (BeansException ex) {
			logger.info("PropertyComparator could not access property - treating as null for sorting", ex);
			return null;
		}
	}

	//排序方法
	public static void sort(List<?> source, SortDefinition sortDefinition) throws BeansException {
		if (StringUtils.hasText(sortDefinition.getProperty())) {
			Collections.sort(source, new PropertyComparator<Object>(sortDefinition));
		}
	}

	//排序方法
	public static void sort(Object[] source, SortDefinition sortDefinition) throws BeansException {
		if (StringUtils.hasText(sortDefinition.getProperty())) {
			Arrays.sort(source, new PropertyComparator<Object>(sortDefinition));
		}
	}

}
