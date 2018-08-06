package org.springframework.beans.bean.factorybean;

import java.lang.reflect.Field;

import org.springframework.beans.factory.support.autowire.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.support.autowire.BeanNameAware;
import org.springframework.beans.exception.FactoryBeanNotInitializedException;
import org.springframework.beans.bean.InitializingBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public class FieldRetrievingFactoryBean
        implements FactoryBean<Object>, BeanNameAware, BeanClassLoaderAware, InitializingBean {

    private Class<?> targetClass;                                                 //目标类型
    private Object targetObject;                                                  //目标对象
    private String targetField;                                                   //目标字段
    private String staticField;                                                   //静态字段
    private String beanName;                                                      //Bean名称
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();     //类加载器
    private Field fieldObject;                                                    //字段对象

    //设置目标类型
    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    //获取目标类型
    public Class<?> getTargetClass() {
        return targetClass;
    }

    //设置目标对象
    public void setTargetObject(Object targetObject) {
        this.targetObject = targetObject;
    }

    //获取目标对象
    public Object getTargetObject() {
        return this.targetObject;
    }

    //设置目标字段
    public void setTargetField(String targetField) {
        this.targetField = StringUtils.trimAllWhitespace(targetField);
    }

    //获取目标字段
    public String getTargetField() {
        return this.targetField;
    }

    //设置静态字段
    public void setStaticField(String staticField) {
        this.staticField = StringUtils.trimAllWhitespace(staticField);
    }

    //设置Bean名称
    @Override
    public void setBeanName(String beanName) {
        this.beanName = StringUtils.trimAllWhitespace(BeanFactoryUtils.originalBeanName(beanName));
    }

    //设置类加载器
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    //属性设置之后执行
    @Override
    public void afterPropertiesSet() throws ClassNotFoundException, NoSuchFieldException {
        //如果目标类型和目标对象都不为空，抛出异常
        if (this.targetClass != null && this.targetObject != null) {
            throw new IllegalArgumentException("Specify either targetClass or targetObject, not both");
        }

        //如果目标类型和目标对象都为空
        if (this.targetClass == null && this.targetObject == null) {
            //如果目标字段不为空，抛出异常
            if (this.targetField != null) {
                throw new IllegalArgumentException(
                        "Specify targetClass or targetObject in combination with targetField");
            }

            //如果静态字段不为空
            if (this.staticField == null) {
                this.staticField = this.beanName;
            }

            //最后一个"."的位置
            int lastDotIndex = this.staticField.lastIndexOf('.');
            //若位置索引不符合要求，则抛出异常
            if (lastDotIndex == -1 || lastDotIndex == this.staticField.length()) {
                throw new IllegalArgumentException(
                        "staticField must be a fully qualified class plus static field name: " +
                                "e.g. 'example.MyExampleClass.MY_EXAMPLE_FIELD'");
            }
            //获取类名
            String className = this.staticField.substring(0, lastDotIndex);
            //获取字段名
            String fieldName = this.staticField.substring(lastDotIndex + 1);
            //设置目标类型
            this.targetClass = ClassUtils.forName(className, this.beanClassLoader);
            //设置目标字段
            this.targetField = fieldName;
        } else if (this.targetField == null) {
            throw new IllegalArgumentException("targetField is required");
        }

        //若目标对象不为空，则先获取目标对象的类型
        Class<?> targetClass = (this.targetObject != null) ? this.targetObject.getClass() : this.targetClass;
        this.fieldObject = targetClass.getField(this.targetField);
    }

    //获取对象
    @Override
    public Object getObject() throws IllegalAccessException {
        if (this.fieldObject == null) {
            throw new FactoryBeanNotInitializedException();
        }
        ReflectionUtils.makeAccessible(this.fieldObject);
        if (this.targetObject != null) {
            // instance field
            return this.fieldObject.get(this.targetObject);
        } else {
            // class field
            return this.fieldObject.get(null);
        }
    }

    //获取对象类型
    @Override
    public Class<?> getObjectType() {
        return (this.fieldObject != null ? this.fieldObject.getType() : null);
    }

    //是否是单例
    @Override
    public boolean isSingleton() {
        return false;
    }

}
