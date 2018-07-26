package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;

//Bean工厂工具
public abstract class BeanFactoryUtils {

    //生成的Bean名称分隔符
    public static final String GENERATED_BEAN_NAME_SEPARATOR = "#";

    //给定名称是否是工厂名称
    public static boolean isFactoryDereference(String name) {
        return (name != null && name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
    }

    //转换成Bean的名称
    public static String transformedBeanName(String name) {
        Assert.notNull(name, "'name' must not be null");
        String beanName = name;
        //截取工厂Bean前缀
        while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
            beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
        }
        return beanName;
    }

    //给定名称是否是已生成的Bean名称
    public static boolean isGeneratedBeanName(String name) {
        return (name != null && name.contains(GENERATED_BEAN_NAME_SEPARATOR));
    }

    //原始化Bean名称
    public static String originalBeanName(String name) {
        Assert.notNull(name, "'name' must not be null");
        int separatorIndex = name.indexOf(GENERATED_BEAN_NAME_SEPARATOR);
        return (separatorIndex != -1 ? name.substring(0, separatorIndex) : name);
    }


    /**
     * Count all beans in any hierarchy in which this factory participates.
     * Includes counts of ancestor bean factories.
     * <p>Beans that are "overridden" (specified in a descendant factory
     * with the same name) are only counted once.
     *
     * @param lbf the bean factory
     * @return count of beans including those defined in ancestor factories
     */
    public static int countBeansIncludingAncestors(ListableBeanFactory lbf) {
        return beanNamesIncludingAncestors(lbf).length;
    }

    /**
     * Return all bean names in the factory, including ancestor factories.
     *
     * @param lbf the bean factory
     * @return the array of matching bean names, or an empty array if none
     * @see #beanNamesForTypeIncludingAncestors
     */
    public static String[] beanNamesIncludingAncestors(ListableBeanFactory lbf) {
        return beanNamesForTypeIncludingAncestors(lbf, Object.class);
    }

    /**
     * Get all bean names for the given type, including those defined in ancestor
     * factories. Will return unique names in case of overridden bean definitions.
     * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
     * will get initialized. If the object created by the FactoryBean doesn't match,
     * the raw FactoryBean itself will be matched against the type.
     * <p>This version of {@code beanNamesForTypeIncludingAncestors} automatically
     * includes prototypes and FactoryBeans.
     *
     * @param lbf  the bean factory
     * @param type the type that beans must match (as a {@code ResolvableType})
     * @return the array of matching bean names, or an empty array if none
     * @since 4.2
     */
    public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, ResolvableType type) {
        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        String[] result = lbf.getBeanNamesForType(type);
        if (lbf instanceof HierarchicalBeanFactory) {
            HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
                String[] parentResult = beanNamesForTypeIncludingAncestors(
                        (ListableBeanFactory) hbf.getParentBeanFactory(), type);
                List<String> resultList = new ArrayList<String>();
                resultList.addAll(Arrays.asList(result));
                for (String beanName : parentResult) {
                    if (!resultList.contains(beanName) && !hbf.containsLocalBean(beanName)) {
                        resultList.add(beanName);
                    }
                }
                result = StringUtils.toStringArray(resultList);
            }
        }
        return result;
    }

    /**
     * Get all bean names for the given type, including those defined in ancestor
     * factories. Will return unique names in case of overridden bean definitions.
     * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
     * will get initialized. If the object created by the FactoryBean doesn't match,
     * the raw FactoryBean itself will be matched against the type.
     * <p>This version of {@code beanNamesForTypeIncludingAncestors} automatically
     * includes prototypes and FactoryBeans.
     *
     * @param lbf  the bean factory
     * @param type the type that beans must match (as a {@code Class})
     * @return the array of matching bean names, or an empty array if none
     */
    public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, Class<?> type) {
        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        String[] result = lbf.getBeanNamesForType(type);
        if (lbf instanceof HierarchicalBeanFactory) {
            HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
                String[] parentResult = beanNamesForTypeIncludingAncestors(
                        (ListableBeanFactory) hbf.getParentBeanFactory(), type);
                List<String> resultList = new ArrayList<String>();
                resultList.addAll(Arrays.asList(result));
                for (String beanName : parentResult) {
                    if (!resultList.contains(beanName) && !hbf.containsLocalBean(beanName)) {
                        resultList.add(beanName);
                    }
                }
                result = StringUtils.toStringArray(resultList);
            }
        }
        return result;
    }

    /**
     * Get all bean names for the given type, including those defined in ancestor
     * factories. Will return unique names in case of overridden bean definitions.
     * <p>Does consider objects created by FactoryBeans if the "allowEagerInit"
     * flag is set, which means that FactoryBeans will get initialized. If the
     * object created by the FactoryBean doesn't match, the raw FactoryBean itself
     * will be matched against the type. If "allowEagerInit" is not set,
     * only raw FactoryBeans will be checked (which doesn't require initialization
     * of each FactoryBean).
     *
     * @param lbf                  the bean factory
     * @param includeNonSingletons whether to include prototype or scoped beans too
     *                             or just singletons (also applies to FactoryBeans)
     * @param allowEagerInit       whether to initialize <i>lazy-init singletons</i> and
     *                             <i>objects created by FactoryBeans</i> (or by factory methods with a
     *                             "factory-bean" reference) for the type check. Note that FactoryBeans need to be
     *                             eagerly initialized to determine their type: So be aware that passing in "true"
     *                             for this flag will initialize FactoryBeans and "factory-bean" references.
     * @param type                 the type that beans must match
     * @return the array of matching bean names, or an empty array if none
     */
    public static String[] beanNamesForTypeIncludingAncestors(
            ListableBeanFactory lbf, Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {

        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        String[] result = lbf.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
        if (lbf instanceof HierarchicalBeanFactory) {
            HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
                String[] parentResult = beanNamesForTypeIncludingAncestors(
                        (ListableBeanFactory) hbf.getParentBeanFactory(), type, includeNonSingletons, allowEagerInit);
                List<String> resultList = new ArrayList<String>();
                resultList.addAll(Arrays.asList(result));
                for (String beanName : parentResult) {
                    if (!resultList.contains(beanName) && !hbf.containsLocalBean(beanName)) {
                        resultList.add(beanName);
                    }
                }
                result = StringUtils.toStringArray(resultList);
            }
        }
        return result;
    }

    /**
     * Return all beans of the given type or subtypes, also picking up beans defined in
     * ancestor bean factories if the current bean factory is a HierarchicalBeanFactory.
     * The returned Map will only contain beans of this type.
     * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
     * will get initialized. If the object created by the FactoryBean doesn't match,
     * the raw FactoryBean itself will be matched against the type.
     * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
     * i.e. such beans will be returned from the lowest factory that they are being found in,
     * hiding corresponding beans in ancestor factories.</b> This feature allows for
     * 'replacing' beans by explicitly choosing the same bean name in a child factory;
     * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
     *
     * @param lbf  the bean factory
     * @param type type of bean to match
     * @return the Map of matching bean instances, or an empty Map if none
     * @throws BeansException if a bean could not be created
     */
    public static <T> Map<String, T> beansOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type)
            throws BeansException {

        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        Map<String, T> result = new LinkedHashMap<String, T>(4);
        result.putAll(lbf.getBeansOfType(type));
        if (lbf instanceof HierarchicalBeanFactory) {
            HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
                Map<String, T> parentResult = beansOfTypeIncludingAncestors(
                        (ListableBeanFactory) hbf.getParentBeanFactory(), type);
                for (Map.Entry<String, T> entry : parentResult.entrySet()) {
                    String beanName = entry.getKey();
                    if (!result.containsKey(beanName) && !hbf.containsLocalBean(beanName)) {
                        result.put(beanName, entry.getValue());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Return all beans of the given type or subtypes, also picking up beans defined in
     * ancestor bean factories if the current bean factory is a HierarchicalBeanFactory.
     * The returned Map will only contain beans of this type.
     * <p>Does consider objects created by FactoryBeans if the "allowEagerInit" flag is set,
     * which means that FactoryBeans will get initialized. If the object created by the
     * FactoryBean doesn't match, the raw FactoryBean itself will be matched against the
     * type. If "allowEagerInit" is not set, only raw FactoryBeans will be checked
     * (which doesn't require initialization of each FactoryBean).
     * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
     * i.e. such beans will be returned from the lowest factory that they are being found in,
     * hiding corresponding beans in ancestor factories.</b> This feature allows for
     * 'replacing' beans by explicitly choosing the same bean name in a child factory;
     * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
     *
     * @param lbf                  the bean factory
     * @param type                 type of bean to match
     * @param includeNonSingletons whether to include prototype or scoped beans too
     *                             or just singletons (also applies to FactoryBeans)
     * @param allowEagerInit       whether to initialize <i>lazy-init singletons</i> and
     *                             <i>objects created by FactoryBeans</i> (or by factory methods with a
     *                             "factory-bean" reference) for the type check. Note that FactoryBeans need to be
     *                             eagerly initialized to determine their type: So be aware that passing in "true"
     *                             for this flag will initialize FactoryBeans and "factory-bean" references.
     * @return the Map of matching bean instances, or an empty Map if none
     * @throws BeansException if a bean could not be created
     */
    public static <T> Map<String, T> beansOfTypeIncludingAncestors(
            ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
            throws BeansException {

        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        Map<String, T> result = new LinkedHashMap<String, T>(4);
        result.putAll(lbf.getBeansOfType(type, includeNonSingletons, allowEagerInit));
        if (lbf instanceof HierarchicalBeanFactory) {
            HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
            if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
                Map<String, T> parentResult = beansOfTypeIncludingAncestors(
                        (ListableBeanFactory) hbf.getParentBeanFactory(), type, includeNonSingletons, allowEagerInit);
                for (Map.Entry<String, T> entry : parentResult.entrySet()) {
                    String beanName = entry.getKey();
                    if (!result.containsKey(beanName) && !hbf.containsLocalBean(beanName)) {
                        result.put(beanName, entry.getValue());
                    }
                }
            }
        }
        return result;
    }


    /**
     * Return a single bean of the given type or subtypes, also picking up beans
     * defined in ancestor bean factories if the current bean factory is a
     * HierarchicalBeanFactory. Useful convenience method when we expect a
     * single bean and don't care about the bean name.
     * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
     * will get initialized. If the object created by the FactoryBean doesn't match,
     * the raw FactoryBean itself will be matched against the type.
     * <p>This version of {@code beanOfTypeIncludingAncestors} automatically includes
     * prototypes and FactoryBeans.
     * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
     * i.e. such beans will be returned from the lowest factory that they are being found in,
     * hiding corresponding beans in ancestor factories.</b> This feature allows for
     * 'replacing' beans by explicitly choosing the same bean name in a child factory;
     * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
     *
     * @param lbf  the bean factory
     * @param type type of bean to match
     * @return the matching bean instance
     * @throws NoSuchBeanDefinitionException   if no bean of the given type was found
     * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
     * @throws BeansException                  if the bean could not be created
     */
    public static <T> T beanOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type)
            throws BeansException {

        Map<String, T> beansOfType = beansOfTypeIncludingAncestors(lbf, type);
        return uniqueBean(type, beansOfType);
    }

    /**
     * Return a single bean of the given type or subtypes, also picking up beans
     * defined in ancestor bean factories if the current bean factory is a
     * HierarchicalBeanFactory. Useful convenience method when we expect a
     * single bean and don't care about the bean name.
     * <p>Does consider objects created by FactoryBeans if the "allowEagerInit" flag is set,
     * which means that FactoryBeans will get initialized. If the object created by the
     * FactoryBean doesn't match, the raw FactoryBean itself will be matched against the
     * type. If "allowEagerInit" is not set, only raw FactoryBeans will be checked
     * (which doesn't require initialization of each FactoryBean).
     * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
     * i.e. such beans will be returned from the lowest factory that they are being found in,
     * hiding corresponding beans in ancestor factories.</b> This feature allows for
     * 'replacing' beans by explicitly choosing the same bean name in a child factory;
     * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
     *
     * @param lbf                  the bean factory
     * @param type                 type of bean to match
     * @param includeNonSingletons whether to include prototype or scoped beans too
     *                             or just singletons (also applies to FactoryBeans)
     * @param allowEagerInit       whether to initialize <i>lazy-init singletons</i> and
     *                             <i>objects created by FactoryBeans</i> (or by factory methods with a
     *                             "factory-bean" reference) for the type check. Note that FactoryBeans need to be
     *                             eagerly initialized to determine their type: So be aware that passing in "true"
     *                             for this flag will initialize FactoryBeans and "factory-bean" references.
     * @return the matching bean instance
     * @throws NoSuchBeanDefinitionException   if no bean of the given type was found
     * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
     * @throws BeansException                  if the bean could not be created
     */
    public static <T> T beanOfTypeIncludingAncestors(
            ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
            throws BeansException {

        Map<String, T> beansOfType = beansOfTypeIncludingAncestors(lbf, type, includeNonSingletons, allowEagerInit);
        return uniqueBean(type, beansOfType);
    }

    /**
     * Return a single bean of the given type or subtypes, not looking in ancestor
     * factories. Useful convenience method when we expect a single bean and
     * don't care about the bean name.
     * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
     * will get initialized. If the object created by the FactoryBean doesn't match,
     * the raw FactoryBean itself will be matched against the type.
     * <p>This version of {@code beanOfType} automatically includes
     * prototypes and FactoryBeans.
     *
     * @param lbf  the bean factory
     * @param type type of bean to match
     * @return the matching bean instance
     * @throws NoSuchBeanDefinitionException   if no bean of the given type was found
     * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
     * @throws BeansException                  if the bean could not be created
     */
    public static <T> T beanOfType(ListableBeanFactory lbf, Class<T> type) throws BeansException {
        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        Map<String, T> beansOfType = lbf.getBeansOfType(type);
        return uniqueBean(type, beansOfType);
    }

    /**
     * Return a single bean of the given type or subtypes, not looking in ancestor
     * factories. Useful convenience method when we expect a single bean and
     * don't care about the bean name.
     * <p>Does consider objects created by FactoryBeans if the "allowEagerInit"
     * flag is set, which means that FactoryBeans will get initialized. If the
     * object created by the FactoryBean doesn't match, the raw FactoryBean itself
     * will be matched against the type. If "allowEagerInit" is not set,
     * only raw FactoryBeans will be checked (which doesn't require initialization
     * of each FactoryBean).
     *
     * @param lbf                  the bean factory
     * @param type                 type of bean to match
     * @param includeNonSingletons whether to include prototype or scoped beans too
     *                             or just singletons (also applies to FactoryBeans)
     * @param allowEagerInit       whether to initialize <i>lazy-init singletons</i> and
     *                             <i>objects created by FactoryBeans</i> (or by factory methods with a
     *                             "factory-bean" reference) for the type check. Note that FactoryBeans need to be
     *                             eagerly initialized to determine their type: So be aware that passing in "true"
     *                             for this flag will initialize FactoryBeans and "factory-bean" references.
     * @return the matching bean instance
     * @throws NoSuchBeanDefinitionException   if no bean of the given type was found
     * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
     * @throws BeansException                  if the bean could not be created
     */
    public static <T> T beanOfType(
            ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
            throws BeansException {

        Assert.notNull(lbf, "ListableBeanFactory must not be null");
        Map<String, T> beansOfType = lbf.getBeansOfType(type, includeNonSingletons, allowEagerInit);
        return uniqueBean(type, beansOfType);
    }

    /**
     * Extract a unique bean for the given type from the given Map of matching beans.
     *
     * @param type          type of bean to match
     * @param matchingBeans all matching beans found
     * @return the unique bean instance
     * @throws NoSuchBeanDefinitionException   if no bean of the given type was found
     * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
     */
    private static <T> T uniqueBean(Class<T> type, Map<String, T> matchingBeans) {
        int nrFound = matchingBeans.size();
        if (nrFound == 1) {
            return matchingBeans.values().iterator().next();
        } else if (nrFound > 1) {
            throw new NoUniqueBeanDefinitionException(type, matchingBeans.keySet());
        } else {
            throw new NoSuchBeanDefinitionException(type);
        }
    }

}
