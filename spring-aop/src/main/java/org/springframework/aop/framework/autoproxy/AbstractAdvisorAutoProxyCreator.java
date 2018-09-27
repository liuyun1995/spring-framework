package org.springframework.aop.framework.autoproxy;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.util.List;

@SuppressWarnings("serial")
public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {

    private BeanFactoryAdvisorRetrievalHelper advisorRetrievalHelper;

    //设置Bean工厂
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            throw new IllegalArgumentException(
                    "AdvisorAutoProxyCreator requires a ConfigurableListableBeanFactory: " + beanFactory);
        }
        initBeanFactory((ConfigurableListableBeanFactory) beanFactory);
    }

    //初始化Bean工厂
    protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory);
    }


    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource targetSource) {
        List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
        if (advisors.isEmpty()) {
            return DO_NOT_PROXY;
        }
        return advisors.toArray();
    }

    /**
     * Find all eligible Advisors for auto-proxying this class.
     *
     * @param beanClass the clazz to find advisors for
     * @param beanName  the name of the currently proxied bean
     * @return the empty List, not {@code null},
     * if there are no pointcuts or interceptors
     * @see #findCandidateAdvisors
     * @see #sortAdvisors
     * @see #extendAdvisors
     */
    protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
        List<Advisor> candidateAdvisors = findCandidateAdvisors();
        List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
        extendAdvisors(eligibleAdvisors);
        if (!eligibleAdvisors.isEmpty()) {
            eligibleAdvisors = sortAdvisors(eligibleAdvisors);
        }
        return eligibleAdvisors;
    }

    /**
     * Find all candidate Advisors to use in auto-proxying.
     *
     * @return the List of candidate Advisors
     */
    protected List<Advisor> findCandidateAdvisors() {
        return this.advisorRetrievalHelper.findAdvisorBeans();
    }

    /**
     * Search the given candidate Advisors to find all Advisors that
     * can apply to the specified bean.
     *
     * @param candidateAdvisors the candidate Advisors
     * @param beanClass         the target's bean class
     * @param beanName          the target's bean name
     * @return the List of applicable Advisors
     * @see ProxyCreationContext#getCurrentProxiedBeanName()
     */
    protected List<Advisor> findAdvisorsThatCanApply(
            List<Advisor> candidateAdvisors, Class<?> beanClass, String beanName) {

        ProxyCreationContext.setCurrentProxiedBeanName(beanName);
        try {
            return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
        } finally {
            ProxyCreationContext.setCurrentProxiedBeanName(null);
        }
    }

    /**
     * Return whether the Advisor bean with the given name is eligible
     * for proxying in the first place.
     *
     * @param beanName the name of the Advisor bean
     * @return whether the bean is eligible
     */
    protected boolean isEligibleAdvisorBean(String beanName) {
        return true;
    }

    /**
     * Sort advisors based on ordering. Subclasses may choose to override this
     * method to customize the sorting strategy.
     *
     * @param advisors the source List of Advisors
     * @return the sorted List of Advisors
     * @see org.springframework.core.Ordered
     * @see org.springframework.core.annotation.Order
     * @see org.springframework.core.annotation.AnnotationAwareOrderComparator
     */
    protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
        AnnotationAwareOrderComparator.sort(advisors);
        return advisors;
    }

    protected void extendAdvisors(List<Advisor> candidateAdvisors) {}

    @Override
    protected boolean advisorsPreFiltered() {
        return true;
    }

    /**
     * Subclass of BeanFactoryAdvisorRetrievalHelper that delegates to
     * surrounding AbstractAdvisorAutoProxyCreator facilities.
     */
    private class BeanFactoryAdvisorRetrievalHelperAdapter extends BeanFactoryAdvisorRetrievalHelper {

        public BeanFactoryAdvisorRetrievalHelperAdapter(ConfigurableListableBeanFactory beanFactory) {
            super(beanFactory);
        }

        @Override
        protected boolean isEligibleBean(String beanName) {
            return AbstractAdvisorAutoProxyCreator.this.isEligibleAdvisorBean(beanName);
        }
    }

}
