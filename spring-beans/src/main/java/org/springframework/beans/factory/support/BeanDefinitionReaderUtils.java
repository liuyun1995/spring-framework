package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

//Bean定义解析器工具
public class BeanDefinitionReaderUtils {

    //生成的Bean名称分隔符
    public static final String GENERATED_BEAN_NAME_SEPARATOR = BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR;

    //创建Bean定义
    public static AbstractBeanDefinition createBeanDefinition(
            String parentName, String className, ClassLoader classLoader) throws ClassNotFoundException {
        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setParentName(parentName);
        if (className != null) {
            if (classLoader != null) {
                bd.setBeanClass(ClassUtils.forName(className, classLoader));
            } else {
                bd.setBeanClassName(className);
            }
        }
        return bd;
    }

    //生成Bean名称
    public static String generateBeanName(BeanDefinition beanDefinition, BeanDefinitionRegistry registry)
            throws BeanDefinitionStoreException {
        return generateBeanName(beanDefinition, registry, false);
    }

    //生成Bean名称
    public static String generateBeanName(
            BeanDefinition definition, BeanDefinitionRegistry registry, boolean isInnerBean)
            throws BeanDefinitionStoreException {
        //获取Bean类型名称
        String generatedBeanName = definition.getBeanClassName();
        //如果Bean的类型名称为空
        if (generatedBeanName == null) {
            //如果Bean的父类名称不为空
            if (definition.getParentName() != null) {
                generatedBeanName = definition.getParentName() + "$child";
            //如果Bean的工厂Bean名称不为空
            } else if (definition.getFactoryBeanName() != null) {
                generatedBeanName = definition.getFactoryBeanName() + "$created";
            }
        }
        if (!StringUtils.hasText(generatedBeanName)) {
            throw new BeanDefinitionStoreException("Unnamed bean definition specifies neither " +
                    "'class' nor 'parent' nor 'factory-bean' - can't generate bean name");
        }

        String id = generatedBeanName;
        //判断是否内部类Bean
        if (isInnerBean) {
            id = generatedBeanName + GENERATED_BEAN_NAME_SEPARATOR + ObjectUtils.getIdentityHexString(definition);
        } else {
            //生成同一类的Bean，名称尾部以序号递增
            int counter = -1;
            while (counter == -1 || registry.containsBeanDefinition(id)) {
                counter++;
                id = generatedBeanName + GENERATED_BEAN_NAME_SEPARATOR + counter;
            }
        }
        return id;
    }

    //注册Bean定义
    public static void registerBeanDefinition(
            BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
            throws BeanDefinitionStoreException {
        //获取Bean名称
        String beanName = definitionHolder.getBeanName();
        //调用注册器方法注册Bean定义
        registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());
        //获取别名数组
        String[] aliases = definitionHolder.getAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                //调用注册器方法进行别名注册
                registry.registerAlias(beanName, alias);
            }
        }
    }

    //使用生成的Bean名称注册
    public static String registerWithGeneratedName(
            AbstractBeanDefinition definition, BeanDefinitionRegistry registry)
            throws BeanDefinitionStoreException {
        //生成Bean名称
        String generatedName = generateBeanName(definition, registry, false);
        //调用注册器方法注册Bean定义
        registry.registerBeanDefinition(generatedName, definition);
        //返回生成的Bean名称
        return generatedName;
    }

}
