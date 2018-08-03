package org.springframework.beans.factory.xml;

import org.springframework.beans.exception.BeanDefinitionStoreException;
import org.springframework.beans.factory.bean.definition.BeanDefinition;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderContext;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.bean.definition.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.reader.XmlBeanDefinitionReader;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;

//xml阅读器上下文
public class XmlReaderContext extends ReaderContext {

    private final org.springframework.beans.factory.xml.reader.XmlBeanDefinitionReader reader;                        //Bean定义阅读器
    private final NamespaceHandlerResolver namespaceHandlerResolver;     //名称空间处理器转换器

    //构造器
    public XmlReaderContext(Resource resource, ProblemReporter problemReporter, ReaderEventListener eventListener,
                            SourceExtractor sourceExtractor, org.springframework.beans.factory.xml.reader.XmlBeanDefinitionReader reader,
                            NamespaceHandlerResolver namespaceHandlerResolver) {
        super(resource, problemReporter, eventListener, sourceExtractor);
        this.reader = reader;
        this.namespaceHandlerResolver = namespaceHandlerResolver;
    }

    //获取阅读器
    public final XmlBeanDefinitionReader getReader() {
        return this.reader;
    }

    //获取Bean定义注册器
    public final BeanDefinitionRegistry getRegistry() {
        return this.reader.getRegistry();
    }

    //获取资源加载器
    public final ResourceLoader getResourceLoader() {
        return this.reader.getResourceLoader();
    }

    //获取类加载器
    public final ClassLoader getBeanClassLoader() {
        return this.reader.getBeanClassLoader();
    }

    //获取环境信息
    public final Environment getEnvironment() {
        return this.reader.getEnvironment();
    }

    //获取名称空间处理器转换器
    public final NamespaceHandlerResolver getNamespaceHandlerResolver() {
        return this.namespaceHandlerResolver;
    }

    //生成Bean的名称
    public String generateBeanName(BeanDefinition beanDefinition) {
        return this.reader.getBeanNameGenerator().generateBeanName(beanDefinition, getRegistry());
    }

    //根据生成的名称进行注册
    public String registerWithGeneratedName(BeanDefinition beanDefinition) {
        String generatedName = generateBeanName(beanDefinition);
        getRegistry().registerBeanDefinition(generatedName, beanDefinition);
        return generatedName;
    }

    //从字符串中读取文档
    public Document readDocumentFromString(String documentContent) {
        InputSource is = new InputSource(new StringReader(documentContent));
        try {
            return this.reader.doLoadDocument(is, getResource());
        } catch (Exception ex) {
            throw new BeanDefinitionStoreException("Failed to read XML document", ex);
        }
    }

}
