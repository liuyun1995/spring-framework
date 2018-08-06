package org.springframework.beans.factory.config;

import java.beans.PropertyEditor;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.ConfigurableListableBeanFactory;
import org.springframework.beans.support.processor.BeanFactoryPostProcessor;
import org.springframework.beans.property.PropertyEditorRegistrar;
import org.springframework.core.Ordered;

//外部编辑器配置器
public class CustomEditorConfigurer implements BeanFactoryPostProcessor, Ordered {

    protected final Log logger = LogFactory.getLog(getClass());
    private int order = Ordered.LOWEST_PRECEDENCE;
    private PropertyEditorRegistrar[] propertyEditorRegistrars;
    private Map<Class<?>, Class<? extends PropertyEditor>> customEditors;

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setPropertyEditorRegistrars(PropertyEditorRegistrar[] propertyEditorRegistrars) {
        this.propertyEditorRegistrars = propertyEditorRegistrars;
    }

    public void setCustomEditors(Map<Class<?>, Class<? extends PropertyEditor>> customEditors) {
        this.customEditors = customEditors;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (this.propertyEditorRegistrars != null) {
            for (PropertyEditorRegistrar propertyEditorRegistrar : this.propertyEditorRegistrars) {
                beanFactory.addPropertyEditorRegistrar(propertyEditorRegistrar);
            }
        }
        if (this.customEditors != null) {
            for (Map.Entry<Class<?>, Class<? extends PropertyEditor>> entry : this.customEditors.entrySet()) {
                Class<?> requiredType = entry.getKey();
                Class<? extends PropertyEditor> propertyEditorClass = entry.getValue();
                beanFactory.registerCustomEditor(requiredType, propertyEditorClass);
            }
        }
    }

}
