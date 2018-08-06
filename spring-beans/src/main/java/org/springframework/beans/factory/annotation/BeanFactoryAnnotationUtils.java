package org.springframework.beans.factory.annotation;

import org.springframework.beans.exception.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.exception.NoSuchBeanDefinitionException;
import org.springframework.beans.exception.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.bean.definition.BeanDefinition;
import org.springframework.beans.factory.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.bean.definition.AbstractBeanDefinition;
import org.springframework.beans.factory.support.autowire.AutowireCandidateQualifier;
import org.springframework.beans.factory.bean.definition.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;

public abstract class BeanFactoryAnnotationUtils {

    public static <T> T qualifiedBeanOfType(BeanFactory beanFactory, Class<T> beanType, String qualifier)
            throws BeansException {
        Assert.notNull(beanFactory, "BeanFactory must not be null");
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            // Full qualifier matching supported.
            return qualifiedBeanOfType((ConfigurableListableBeanFactory) beanFactory, beanType, qualifier);
        } else if (beanFactory.containsBean(qualifier)) {
            // Fallback: target bean at least found by bean name.
            return beanFactory.getBean(qualifier, beanType);
        } else {
            throw new NoSuchBeanDefinitionException(qualifier, "No matching " + beanType.getSimpleName() +
                    " bean found for bean name '" + qualifier +
                    "'! (Note: Qualifier matching not supported because given " +
                    "BeanFactory does not implement ConfigurableListableBeanFactory.)");
        }
    }

    private static <T> T qualifiedBeanOfType(ConfigurableListableBeanFactory bf, Class<T> beanType, String qualifier) {
        String[] candidateBeans = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(bf, beanType);
        String matchingBean = null;
        for (String beanName : candidateBeans) {
            if (isQualifierMatch(qualifier, beanName, bf)) {
                if (matchingBean != null) {
                    throw new NoUniqueBeanDefinitionException(beanType, matchingBean, beanName);
                }
                matchingBean = beanName;
            }
        }
        if (matchingBean != null) {
            return bf.getBean(matchingBean, beanType);
        } else if (bf.containsBean(qualifier)) {
            // Fallback: target bean at least found by bean name - probably a manually registered singleton.
            return bf.getBean(qualifier, beanType);
        } else {
            throw new NoSuchBeanDefinitionException(qualifier, "No matching " + beanType.getSimpleName() +
                    " bean found for qualifier '" + qualifier + "' - neither qualifier match nor bean name match!");
        }
    }

    private static boolean isQualifierMatch(String qualifier, String beanName, ConfigurableListableBeanFactory bf) {
        if (bf.containsBean(beanName)) {
            try {
                BeanDefinition bd = bf.getMergedBeanDefinition(beanName);
                // Explicit qualifier metadata on bean definition? (typically in XML definition)
                if (bd instanceof AbstractBeanDefinition) {
                    AbstractBeanDefinition abd = (AbstractBeanDefinition) bd;
                    AutowireCandidateQualifier candidate = abd.getQualifier(Qualifier.class.getName());
                    if ((candidate != null && qualifier.equals(candidate.getAttribute(AutowireCandidateQualifier.VALUE_KEY))) ||
                            qualifier.equals(beanName) || ObjectUtils.containsElement(bf.getAliases(beanName), qualifier)) {
                        return true;
                    }
                }
                // Corresponding qualifier on factory method? (typically in configuration class)
                if (bd instanceof RootBeanDefinition) {
                    Method factoryMethod = ((RootBeanDefinition) bd).getResolvedFactoryMethod();
                    if (factoryMethod != null) {
                        Qualifier targetAnnotation = AnnotationUtils.getAnnotation(factoryMethod, Qualifier.class);
                        if (targetAnnotation != null) {
                            return qualifier.equals(targetAnnotation.value());
                        }
                    }
                }
                // Corresponding qualifier on bean implementation class? (for custom user types)
                Class<?> beanType = bf.getType(beanName);
                if (beanType != null) {
                    Qualifier targetAnnotation = AnnotationUtils.getAnnotation(beanType, Qualifier.class);
                    if (targetAnnotation != null) {
                        return qualifier.equals(targetAnnotation.value());
                    }
                }
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore - can't compare qualifiers for a manually registered singleton object
            }
        }
        return false;
    }

}
