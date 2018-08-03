package org.springframework.beans.factory.xml.parser;

import org.w3c.dom.Element;

import org.springframework.beans.exception.BeanDefinitionStoreException;
import org.springframework.beans.factory.bean.definition.BeanDefinition;
import org.springframework.beans.factory.bean.definition.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.bean.definition.AbstractBeanDefinition;
import org.springframework.beans.factory.bean.definition.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.bean.definition.BeanDefinitionRegistry;
import org.springframework.util.StringUtils;

//抽象Bean定义解析器
public abstract class AbstractBeanDefinitionParser implements BeanDefinitionParser {

    public static final String ID_ATTRIBUTE = "id";
    public static final String NAME_ATTRIBUTE = "name";

    //解析方法
    @Override
    public final BeanDefinition parse(Element element, ParserContext parserContext) {
        //获取抽象Bean定义
        AbstractBeanDefinition definition = parseInternal(element, parserContext);
        if (definition != null && !parserContext.isNested()) {
            try {
                //获取ID值
                String id = resolveId(element, definition, parserContext);
                if (!StringUtils.hasText(id)) {
                    parserContext.getReaderContext().error(
                            "Id is required for element '" + parserContext.getDelegate().getLocalName(element)
                                    + "' when used as a top-level tag", element);
                }
                String[] aliases = null;
                //判断是否要解析名称作为别名
                if (shouldParseNameAsAliases()) {
                    //获取节点的name属性
                    String name = element.getAttribute(NAME_ATTRIBUTE);
                    //如果name属性不为空
                    if (StringUtils.hasLength(name)) {
                        //将解析后的数组设置为别名数组
                        aliases = StringUtils.trimArrayElements(StringUtils.commaDelimitedListToStringArray(name));
                    }
                }
                //创建Bean定义持有器
                BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, id, aliases);
                //注册Bean定义
                registerBeanDefinition(holder, parserContext.getRegistry());
                //判断是否要移除事件
                if (shouldFireEvents()) {
                    //新建Bean组件定义
                    BeanComponentDefinition componentDefinition = new BeanComponentDefinition(holder);
                    //执行勾子方法
                    postProcessComponentDefinition(componentDefinition);
                    //注册组件定义
                    parserContext.registerComponent(componentDefinition);
                }
            } catch (BeanDefinitionStoreException ex) {
                parserContext.getReaderContext().error(ex.getMessage(), element);
                return null;
            }
        }
        //最后返回Bean定义
        return definition;
    }

    //解析id
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
            throws BeanDefinitionStoreException {
        //判断是否要生成id
        if (shouldGenerateId()) {
            return parserContext.getReaderContext().generateBeanName(definition);
        } else {
            //获取节点的id属性
            String id = element.getAttribute(ID_ATTRIBUTE);
            //若id属性为空，则再次判断是否要生成id
            if (!StringUtils.hasText(id) && shouldGenerateIdAsFallback()) {
                id = parserContext.getReaderContext().generateBeanName(definition);
            }
            //否则直接返回该id
            return id;
        }
    }

    //注册Bean定义
    protected void registerBeanDefinition(BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
        BeanDefinitionReaderUtils.registerBeanDefinition(definition, registry);
    }

    //解析内部类Bean定义
    protected abstract AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext);

    //是否要生成ID
    protected boolean shouldGenerateId() {
        return false;
    }

    //是否要生成ID(若id属性值为空)
    protected boolean shouldGenerateIdAsFallback() {
        return false;
    }

    //是否解析名称作为别名
    protected boolean shouldParseNameAsAliases() {
        return true;
    }

    //是否要解除事件
    protected boolean shouldFireEvents() {
        return true;
    }


    protected void postProcessComponentDefinition(BeanComponentDefinition componentDefinition) { }

}
