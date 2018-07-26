package org.springframework.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

//易变属性值
@SuppressWarnings("serial")
public class MutablePropertyValues implements PropertyValues, Serializable {

	private final List<PropertyValue> propertyValueList;   //属性值列表
	private Set<String> processedProperties;               //已处理属性集合
	private volatile boolean converted = false;            //是否进行过转换

	//构造器1
	public MutablePropertyValues() {
		this.propertyValueList = new ArrayList<PropertyValue>(0);
	}

	//构造器2
	public MutablePropertyValues(PropertyValues original) {
		if (original != null) {
			PropertyValue[] pvs = original.getPropertyValues();
			this.propertyValueList = new ArrayList<PropertyValue>(pvs.length);
			for (PropertyValue pv : pvs) {
				this.propertyValueList.add(new PropertyValue(pv));
			}
		} else {
			this.propertyValueList = new ArrayList<PropertyValue>(0);
		}
	}

	//构造器3
	public MutablePropertyValues(Map<?, ?> original) {
		if (original != null) {
			this.propertyValueList = new ArrayList<PropertyValue>(original.size());
			for (Map.Entry<?, ?> entry : original.entrySet()) {
				this.propertyValueList.add(new PropertyValue(entry.getKey().toString(), entry.getValue()));
			}
		} else {
			this.propertyValueList = new ArrayList<PropertyValue>(0);
		}
	}

	//构造器4
	public MutablePropertyValues(List<PropertyValue> propertyValueList) {
		this.propertyValueList = (propertyValueList != null ? propertyValueList : new ArrayList<PropertyValue>());
	}

	//获取属性值列表
	public List<PropertyValue> getPropertyValueList() {
		return this.propertyValueList;
	}

	//获取属性值列表大小
	public int size() {
		return this.propertyValueList.size();
	}

	//添加属性值
	public MutablePropertyValues addPropertyValues(PropertyValues other) {
		if (other != null) {
			PropertyValue[] pvs = other.getPropertyValues();
			for (PropertyValue pv : pvs) {
				addPropertyValue(new PropertyValue(pv));
			}
		}
		return this;
	}

	//添加属性值
	public MutablePropertyValues addPropertyValues(Map<?, ?> other) {
		if (other != null) {
			for (Map.Entry<?, ?> entry : other.entrySet()) {
				addPropertyValue(new PropertyValue(entry.getKey().toString(), entry.getValue()));
			}
		}
		return this;
	}

	//添加属性值
	public MutablePropertyValues addPropertyValue(PropertyValue pv) {
		//遍历属性值列表，查找是否存在该属性
		for (int i = 0; i < this.propertyValueList.size(); i++) {
			PropertyValue currentPv = this.propertyValueList.get(i);
			//判断当前属性名是否和给定属性名相等
			if (currentPv.getName().equals(pv.getName())) {
				//合并两个属性
				pv = mergeIfRequired(pv, currentPv);
				//该位置的属性更新为合并后的属性
				setPropertyValueAt(pv, i);
				return this;
			}
		}
		//若属性列表没有该属性，则添加进列表
		this.propertyValueList.add(pv);
		return this;
	}

	//添加属性值
	public void addPropertyValue(String propertyName, Object propertyValue) {
		addPropertyValue(new PropertyValue(propertyName, propertyValue));
	}

	//添加属性值
	public MutablePropertyValues add(String propertyName, Object propertyValue) {
		addPropertyValue(new PropertyValue(propertyName, propertyValue));
		return this;
	}

	//设置指定位置的属性值
	public void setPropertyValueAt(PropertyValue pv, int i) {
		this.propertyValueList.set(i, pv);
	}

	//是否要合并
	private PropertyValue mergeIfRequired(PropertyValue newPv, PropertyValue currentPv) {
		Object value = newPv.getValue();
		if (value instanceof Mergeable) {
			Mergeable mergeable = (Mergeable) value;
			if (mergeable.isMergeEnabled()) {
				Object merged = mergeable.merge(currentPv.getValue());
				return new PropertyValue(newPv.getName(), merged);
			}
		}
		return newPv;
	}

	//移除属性值
	public void removePropertyValue(PropertyValue pv) {
		this.propertyValueList.remove(pv);
	}

	//移除属性值
	public void removePropertyValue(String propertyName) {
		this.propertyValueList.remove(getPropertyValue(propertyName));
	}

	//获取全部属性值
	@Override
	public PropertyValue[] getPropertyValues() {
		return this.propertyValueList.toArray(new PropertyValue[this.propertyValueList.size()]);
	}

	//根据名称获取属性值
	@Override
	public PropertyValue getPropertyValue(String propertyName) {
		for (PropertyValue pv : this.propertyValueList) {
			if (pv.getName().equals(propertyName)) {
				return pv;
			}
		}
		return null;
	}

	//根据属性名获取对象
	public Object get(String propertyName) {
		PropertyValue pv = getPropertyValue(propertyName);
		return (pv != null ? pv.getValue() : null);
	}


	@Override
	public PropertyValues changesSince(PropertyValues old) {
		MutablePropertyValues changes = new MutablePropertyValues();
		if (old == this) {
			return changes;
		}

		// for each property value in the new set
		for (PropertyValue newPv : this.propertyValueList) {
			// if there wasn't an old one, add it
			PropertyValue pvOld = old.getPropertyValue(newPv.getName());
			if (pvOld == null) {
				changes.addPropertyValue(newPv);
			} else if (!pvOld.equals(newPv)) {
				// it's changed
				changes.addPropertyValue(newPv);
			}
		}
		return changes;
	}

	//是否包含属性值
	@Override
	public boolean contains(String propertyName) {
		return (getPropertyValue(propertyName) != null
				|| (this.processedProperties != null && this.processedProperties.contains(propertyName)));
	}

	//属性值列表是否为空
	@Override
	public boolean isEmpty() {
		return this.propertyValueList.isEmpty();
	}

	//注册处理过的属性
	public void registerProcessedProperty(String propertyName) {
		if (this.processedProperties == null) {
			this.processedProperties = new HashSet<String>();
		}
		this.processedProperties.add(propertyName);
	}

	//清理处理过的属性
	public void clearProcessedProperty(String propertyName) {
		if (this.processedProperties != null) {
			this.processedProperties.remove(propertyName);
		}
	}

	//设置为已转换
	public void setConverted() {
		this.converted = true;
	}

	//是否已转换
	public boolean isConverted() {
		return this.converted;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MutablePropertyValues)) {
			return false;
		}
		MutablePropertyValues that = (MutablePropertyValues) other;
		return this.propertyValueList.equals(that.propertyValueList);
	}

	@Override
	public int hashCode() {
		return this.propertyValueList.hashCode();
	}

	@Override
	public String toString() {
		PropertyValue[] pvs = getPropertyValues();
		StringBuilder sb = new StringBuilder("PropertyValues: length=").append(pvs.length);
		if (pvs.length > 0) {
			sb.append("; ").append(StringUtils.arrayToDelimitedString(pvs, "; "));
		}
		return sb.toString();
	}

}
