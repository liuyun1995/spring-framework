package org.springframework.beans.factory.parsing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.property.PropertyValue;
import org.springframework.beans.property.PropertyValues;
import org.springframework.beans.factory.bean.definition.BeanDefinition;
import org.springframework.beans.factory.bean.definition.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanReference;

//Bean组件定义
public class BeanComponentDefinition extends BeanDefinitionHolder implements ComponentDefinition {

    private BeanDefinition[] innerBeanDefinitions;  //内联Bean定义集合
    private BeanReference[] beanReferences;         //Bean引用集合

    public BeanComponentDefinition(BeanDefinition beanDefinition, String beanName) {
        super(beanDefinition, beanName);
        findInnerBeanDefinitionsAndBeanReferences(beanDefinition);
    }

    public BeanComponentDefinition(BeanDefinition beanDefinition, String beanName, String[] aliases) {
        super(beanDefinition, beanName, aliases);
        findInnerBeanDefinitionsAndBeanReferences(beanDefinition);
    }

    public BeanComponentDefinition(BeanDefinitionHolder holder) {
        super(holder);
        findInnerBeanDefinitionsAndBeanReferences(holder.getBeanDefinition());
    }

    private void findInnerBeanDefinitionsAndBeanReferences(BeanDefinition beanDefinition) {
        List<BeanDefinition> innerBeans = new ArrayList<BeanDefinition>();
        List<BeanReference> references = new ArrayList<BeanReference>();
        PropertyValues propertyValues = beanDefinition.getPropertyValues();
        for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
            Object value = propertyValue.getValue();
            if (value instanceof BeanDefinitionHolder) {
                innerBeans.add(((BeanDefinitionHolder) value).getBeanDefinition());
            } else if (value instanceof BeanDefinition) {
                innerBeans.add((BeanDefinition) value);
            } else if (value instanceof BeanReference) {
                references.add((BeanReference) value);
            }
        }
        this.innerBeanDefinitions = innerBeans.toArray(new BeanDefinition[innerBeans.size()]);
        this.beanReferences = references.toArray(new BeanReference[references.size()]);
    }

    @Override
    public String getName() {
        return getBeanName();
    }

    @Override
    public String getDescription() {
        return getShortDescription();
    }

    @Override
    public BeanDefinition[] getBeanDefinitions() {
        return new BeanDefinition[]{getBeanDefinition()};
    }

    @Override
    public BeanDefinition[] getInnerBeanDefinitions() {
        return this.innerBeanDefinitions;
    }

    @Override
    public BeanReference[] getBeanReferences() {
        return this.beanReferences;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof BeanComponentDefinition && super.equals(other)));
    }

}
