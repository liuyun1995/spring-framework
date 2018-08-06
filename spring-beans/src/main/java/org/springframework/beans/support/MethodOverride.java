package org.springframework.beans.support;

import java.lang.reflect.Method;

import org.springframework.beans.bean.BeanMetadataElement;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

//方法覆盖
public abstract class MethodOverride implements BeanMetadataElement {

    private final String methodName;
    private boolean overloaded = true;
    private Object source;

    protected MethodOverride(String methodName) {
        Assert.notNull(methodName, "Method name must not be null");
        this.methodName = methodName;
    }

    public String getMethodName() {
        return this.methodName;
    }

    protected void setOverloaded(boolean overloaded) {
        this.overloaded = overloaded;
    }

    protected boolean isOverloaded() {
        return this.overloaded;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    @Override
    public Object getSource() {
        return this.source;
    }

    public abstract boolean matches(Method method);

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MethodOverride)) {
            return false;
        }
        MethodOverride that = (MethodOverride) other;
        return (ObjectUtils.nullSafeEquals(this.methodName, that.methodName) &&
                ObjectUtils.nullSafeEquals(this.source, that.source));
    }

    @Override
    public int hashCode() {
        int hashCode = ObjectUtils.nullSafeHashCode(this.methodName);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.source);
        return hashCode;
    }

}
