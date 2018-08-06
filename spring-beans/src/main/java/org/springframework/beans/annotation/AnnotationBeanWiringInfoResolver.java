package org.springframework.beans.annotation;

import org.springframework.beans.factory.wiring.BeanWiringInfo;
import org.springframework.beans.factory.wiring.BeanWiringInfoResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class AnnotationBeanWiringInfoResolver implements BeanWiringInfoResolver {

    @Override
    public BeanWiringInfo resolveWiringInfo(Object beanInstance) {
        Assert.notNull(beanInstance, "Bean instance must not be null");
        Configurable annotation = beanInstance.getClass().getAnnotation(Configurable.class);
        return (annotation != null ? buildWiringInfo(beanInstance, annotation) : null);
    }

    protected BeanWiringInfo buildWiringInfo(Object beanInstance, Configurable annotation) {
        if (!Autowire.NO.equals(annotation.autowire())) {
            return new BeanWiringInfo(annotation.autowire().value(), annotation.dependencyCheck());
        } else {
            if (!"".equals(annotation.value())) {
                // explicitly specified bean name
                return new BeanWiringInfo(annotation.value(), false);
            } else {
                // default bean name
                return new BeanWiringInfo(getDefaultBeanName(beanInstance), true);
            }
        }
    }


    protected String getDefaultBeanName(Object beanInstance) {
        return ClassUtils.getUserClass(beanInstance).getName();
    }

}
