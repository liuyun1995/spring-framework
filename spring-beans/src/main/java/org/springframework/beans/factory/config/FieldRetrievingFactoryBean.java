package org.springframework.beans.factory.config;

import java.lang.reflect.Field;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public class FieldRetrievingFactoryBean
        implements FactoryBean<Object>, BeanNameAware, BeanClassLoaderAware, InitializingBean {

    private Class<?> targetClass;

    private Object targetObject;

    private String targetField;

    private String staticField;

    private String beanName;

    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    // the field we will retrieve
    private Field fieldObject;


    /**
     * Set the target class on which the field is defined.
     * Only necessary when the target field is static; else,
     * a target object needs to be specified anyway.
     *
     * @see #setTargetObject
     * @see #setTargetField
     */
    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * Return the target class on which the field is defined.
     */
    public Class<?> getTargetClass() {
        return targetClass;
    }

    /**
     * Set the target object on which the field is defined.
     * Only necessary when the target field is not static;
     * else, a target class is sufficient.
     *
     * @see #setTargetClass
     * @see #setTargetField
     */
    public void setTargetObject(Object targetObject) {
        this.targetObject = targetObject;
    }

    /**
     * Return the target object on which the field is defined.
     */
    public Object getTargetObject() {
        return this.targetObject;
    }

    /**
     * Set the name of the field to be retrieved.
     * Refers to either a static field or a non-static field,
     * depending on a target object being set.
     *
     * @see #setTargetClass
     * @see #setTargetObject
     */
    public void setTargetField(String targetField) {
        this.targetField = StringUtils.trimAllWhitespace(targetField);
    }

    /**
     * Return the name of the field to be retrieved.
     */
    public String getTargetField() {
        return this.targetField;
    }

    /**
     * Set a fully qualified static field name to retrieve,
     * e.g. "example.MyExampleClass.MY_EXAMPLE_FIELD".
     * Convenient alternative to specifying targetClass and targetField.
     *
     * @see #setTargetClass
     * @see #setTargetField
     */
    public void setStaticField(String staticField) {
        this.staticField = StringUtils.trimAllWhitespace(staticField);
    }

    /**
     * The bean name of this FieldRetrievingFactoryBean will be interpreted
     * as "staticField" pattern, if neither "targetClass" nor "targetObject"
     * nor "targetField" have been specified.
     * This allows for concise bean definitions with just an id/name.
     */
    @Override
    public void setBeanName(String beanName) {
        this.beanName = StringUtils.trimAllWhitespace(BeanFactoryUtils.originalBeanName(beanName));
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }


    @Override
    public void afterPropertiesSet() throws ClassNotFoundException, NoSuchFieldException {
        if (this.targetClass != null && this.targetObject != null) {
            throw new IllegalArgumentException("Specify either targetClass or targetObject, not both");
        }

        if (this.targetClass == null && this.targetObject == null) {
            if (this.targetField != null) {
                throw new IllegalArgumentException(
                        "Specify targetClass or targetObject in combination with targetField");
            }

            // If no other property specified, consider bean name as static field expression.
            if (this.staticField == null) {
                this.staticField = this.beanName;
            }

            // Try to parse static field into class and field.
            int lastDotIndex = this.staticField.lastIndexOf('.');
            if (lastDotIndex == -1 || lastDotIndex == this.staticField.length()) {
                throw new IllegalArgumentException(
                        "staticField must be a fully qualified class plus static field name: " +
                                "e.g. 'example.MyExampleClass.MY_EXAMPLE_FIELD'");
            }
            String className = this.staticField.substring(0, lastDotIndex);
            String fieldName = this.staticField.substring(lastDotIndex + 1);
            this.targetClass = ClassUtils.forName(className, this.beanClassLoader);
            this.targetField = fieldName;
        } else if (this.targetField == null) {
            // Either targetClass or targetObject specified.
            throw new IllegalArgumentException("targetField is required");
        }

        // Try to get the exact method first.
        Class<?> targetClass = (this.targetObject != null) ? this.targetObject.getClass() : this.targetClass;
        this.fieldObject = targetClass.getField(this.targetField);
    }


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

    @Override
    public Class<?> getObjectType() {
        return (this.fieldObject != null ? this.fieldObject.getType() : null);
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
