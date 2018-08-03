package org.springframework.beans.factory.config;

import java.lang.reflect.InvocationTargetException;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.bean.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.bean.InitializingBean;
import org.springframework.beans.factory.bean.factory.ConfigurableBeanFactory;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
import org.springframework.util.ClassUtils;

public class MethodInvokingBean extends ArgumentConvertingMethodInvoker
        implements BeanClassLoaderAware, BeanFactoryAware, InitializingBean {

    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();  //类型加载器
    private ConfigurableBeanFactory beanFactory;                               //可配置的Bean工厂

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    @Override
    protected Class<?> resolveClassName(String className) throws ClassNotFoundException {
        return ClassUtils.forName(className, this.beanClassLoader);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        }
    }

    //获取类型转换器
    @Override
    protected TypeConverter getDefaultTypeConverter() {
        if (this.beanFactory != null) {
            return this.beanFactory.getTypeConverter();
        } else {
            return super.getDefaultTypeConverter();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        prepare();
        invokeWithTargetException();
    }

    protected Object invokeWithTargetException() throws Exception {
        try {
            return invoke();
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof Exception) {
                throw (Exception) ex.getTargetException();
            }
            if (ex.getTargetException() instanceof Error) {
                throw (Error) ex.getTargetException();
            }
            throw ex;
        }
    }

}
