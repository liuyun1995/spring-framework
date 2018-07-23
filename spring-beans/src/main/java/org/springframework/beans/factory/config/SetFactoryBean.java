package org.springframework.beans.factory.config;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeConverter;
import org.springframework.core.ResolvableType;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Simple factory for shared Set instances. Allows for central setup of Sets via
 * the "set" element in XML bean definitions.
 *
 * @author Juergen Hoeller
 * @see ListFactoryBean
 * @see MapFactoryBean
 * @since 09.12.2003
 */
public class SetFactoryBean extends AbstractFactoryBean<Set<Object>> {

    private Set<?> sourceSet;

    @SuppressWarnings("rawtypes")
    private Class<? extends Set> targetSetClass;

    /**
     * Set the source Set, typically populated via XML "set" elements.
     */
    public void setSourceSet(Set<?> sourceSet) {
        this.sourceSet = sourceSet;
    }

    /**
     * Set the class to use for the target Set. Can be populated with a fully
     * qualified class name when defined in a Spring application context.
     * <p>
     * Default is a linked HashSet, keeping the registration order.
     *
     * @see java.util.LinkedHashSet
     */
    @SuppressWarnings("rawtypes")
    public void setTargetSetClass(Class<? extends Set> targetSetClass) {
        if (targetSetClass == null) {
            throw new IllegalArgumentException("'targetSetClass' must not be null");
        }
        if (!Set.class.isAssignableFrom(targetSetClass)) {
            throw new IllegalArgumentException("'targetSetClass' must implement [java.util.Set]");
        }
        this.targetSetClass = targetSetClass;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<Set> getObjectType() {
        return Set.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Set<Object> createInstance() {
        if (this.sourceSet == null) {
            throw new IllegalArgumentException("'sourceSet' is required");
        }
        Set<Object> result = null;
        if (this.targetSetClass != null) {
            result = BeanUtils.instantiateClass(this.targetSetClass);
        } else {
            result = new LinkedHashSet<Object>(this.sourceSet.size());
        }
        Class<?> valueType = null;
        if (this.targetSetClass != null) {
            valueType = ResolvableType.forClass(this.targetSetClass).asCollection().resolveGeneric();
        }
        if (valueType != null) {
            TypeConverter converter = getBeanTypeConverter();
            for (Object elem : this.sourceSet) {
                result.add(converter.convertIfNecessary(elem, valueType));
            }
        } else {
            result.addAll(this.sourceSet);
        }
        return result;
    }

}
