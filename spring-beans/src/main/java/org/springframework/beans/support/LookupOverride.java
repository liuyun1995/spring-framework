package org.springframework.beans.support;

import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

//扫描覆盖
public class LookupOverride extends MethodOverride {

    private final String beanName;    //Bean名称
    private Method method;            //方法

    //构造器1
    public LookupOverride(String methodName, String beanName) {
        super(methodName);
        this.beanName = beanName;
    }

    //构造器2
    public LookupOverride(Method method, String beanName) {
        super(method.getName());
        this.method = method;
        this.beanName = beanName;
    }

    //获取Bean名称
    public String getBeanName() {
        return this.beanName;
    }

    //匹配方法
    @Override
    public boolean matches(Method method) {
        if (this.method != null) {
            return method.equals(this.method);
        } else {
            return (method.getName().equals(getMethodName()) && (!isOverloaded() ||
                    Modifier.isAbstract(method.getModifiers()) || method.getParameterTypes().length == 0));
        }
    }


    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LookupOverride) || !super.equals(other)) {
            return false;
        }
        LookupOverride that = (LookupOverride) other;
        return (ObjectUtils.nullSafeEquals(this.method, that.method) &&
                ObjectUtils.nullSafeEquals(this.beanName, that.beanName));
    }

    @Override
    public int hashCode() {
        return (29 * super.hashCode() + ObjectUtils.nullSafeHashCode(this.beanName));
    }

    @Override
    public String toString() {
        return "LookupOverride for method '" + getMethodName() + "'";
    }

}
