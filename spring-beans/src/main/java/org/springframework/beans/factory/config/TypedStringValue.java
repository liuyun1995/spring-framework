package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

public class TypedStringValue implements BeanMetadataElement {

    private String value;
    private volatile Object targetType;
    private Object source;
    private String specifiedTypeName;
    private volatile boolean dynamic;

    //构造器1
    public TypedStringValue(String value) {
        setValue(value);
    }

    //构造器2
    public TypedStringValue(String value, Class<?> targetType) {
        setValue(value);
        setTargetType(targetType);
    }

    //构造器3
    public TypedStringValue(String value, String targetTypeName) {
        setValue(value);
        setTargetTypeName(targetTypeName);
    }

    //设置值
    public void setValue(String value) {
        this.value = value;
    }

    //获取值
    public String getValue() {
        return this.value;
    }

    //设置目标类型
    public void setTargetType(Class<?> targetType) {
        Assert.notNull(targetType, "'targetType' must not be null");
        this.targetType = targetType;
    }

    //获取目标类型
    public Class<?> getTargetType() {
        Object targetTypeValue = this.targetType;
        if (!(targetTypeValue instanceof Class)) {
            throw new IllegalStateException("Typed String value does not carry a resolved target type");
        }
        return (Class<?>) targetTypeValue;
    }

    //设置目标类型名
    public void setTargetTypeName(String targetTypeName) {
        Assert.notNull(targetTypeName, "'targetTypeName' must not be null");
        this.targetType = targetTypeName;
    }

    //获取目标类型名
    public String getTargetTypeName() {
        Object targetTypeValue = this.targetType;
        if (targetTypeValue instanceof Class) {
            return ((Class<?>) targetTypeValue).getName();
        } else {
            return (String) targetTypeValue;
        }
    }

    //是否有目标类型
    public boolean hasTargetType() {
        return (this.targetType instanceof Class);
    }

    //解析目标类型
    public Class<?> resolveTargetType(ClassLoader classLoader) throws ClassNotFoundException {
        if (this.targetType == null) {
            return null;
        }
        Class<?> resolvedClass = ClassUtils.forName(getTargetTypeName(), classLoader);
        this.targetType = resolvedClass;
        return resolvedClass;
    }

    //设置源文件
    public void setSource(Object source) {
        this.source = source;
    }

    //获取源文件
    @Override
    public Object getSource() {
        return this.source;
    }

    //设置具体的类型名
    public void setSpecifiedTypeName(String specifiedTypeName) {
        this.specifiedTypeName = specifiedTypeName;
    }

    //获取具体的类型名
    public String getSpecifiedTypeName() {
        return this.specifiedTypeName;
    }

    //设置是否是动态的
    public void setDynamic() {
        this.dynamic = true;
    }

    //是否是动态的
    public boolean isDynamic() {
        return this.dynamic;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TypedStringValue)) {
            return false;
        }
        TypedStringValue otherValue = (TypedStringValue) other;
        return (ObjectUtils.nullSafeEquals(this.value, otherValue.value) &&
                ObjectUtils.nullSafeEquals(this.targetType, otherValue.targetType));
    }

    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(this.value) * 29 + ObjectUtils.nullSafeHashCode(this.targetType);
    }

    @Override
    public String toString() {
        return "TypedStringValue: value [" + this.value + "], target type [" + this.targetType + "]";
    }

}
