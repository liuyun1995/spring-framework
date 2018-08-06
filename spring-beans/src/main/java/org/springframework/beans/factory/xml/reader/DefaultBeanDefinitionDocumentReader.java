package org.springframework.beans.factory.xml.reader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.parser.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.exception.BeanDefinitionStoreException;
import org.springframework.beans.bean.definition.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

//默认Bean定义文档阅读器
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

	public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;

	public static final String NESTED_BEANS_ELEMENT = "beans";
	public static final String ALIAS_ELEMENT = "alias";
	public static final String IMPORT_ELEMENT = "import";

	public static final String NAME_ATTRIBUTE = "name";
	public static final String ALIAS_ATTRIBUTE = "alias";
	public static final String RESOURCE_ATTRIBUTE = "resource";
	public static final String PROFILE_ATTRIBUTE = "profile";

	protected final Log logger = LogFactory.getLog(getClass());    //日志类
	private XmlReaderContext readerContext;                        //XML阅读器上下文
	private BeanDefinitionParserDelegate delegate;                 //Bean定义解析器装饰器

	//注册Bean定义
	@Override
	public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
		//设置解析上下文
		this.readerContext = readerContext;
		logger.debug("Loading bean definitions");
		//获取文档根节点
		Element root = doc.getDocumentElement();
		//从根节点开始解析
		doRegisterBeanDefinitions(root);
	}

	//获取解析上下文
	protected final XmlReaderContext getReaderContext() {
		return this.readerContext;
	}

	//设置额外xml资源
	protected Object extractSource(Element ele) {
		return getReaderContext().extractSource(ele);
	}

	//核心注册Bean定义方法
	protected void doRegisterBeanDefinitions(Element root) {
		//获取当前Bean定义解析器
		BeanDefinitionParserDelegate parent = this.delegate;
		//新建Bean定义解析器，设置父类为当前Bean定义解析器
		this.delegate = createDelegate(getReaderContext(), root, parent);
		//判断节点名称空间是否是默认名称空间
		if (this.delegate.isDefaultNamespace(root)) {
			//获取节点的profile属性
			String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
			if (StringUtils.hasText(profileSpec)) {
				//分割多值属性值
				String[] specifiedProfiles = StringUtils.tokenizeToStringArray(profileSpec,
						BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
				//如果不接受所有profile属性，则打印日志并退出
				if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
					if (logger.isInfoEnabled()) {
						logger.info("Skipped XML bean definition file due to specified profiles [" + profileSpec
								+ "] not matching: " + getReaderContext().getResource());
					}
					return;
				}
			}
		}

		//解析XMl之前做一些事情
		preProcessXml(root);
		//解析Bean定义
		parseBeanDefinitions(root, this.delegate);
		//解析XMl之后做一些事情
		postProcessXml(root);

		this.delegate = parent;
	}

	//创建Bean定义解析器装饰器
	protected BeanDefinitionParserDelegate createDelegate(XmlReaderContext readerContext, Element root,
			BeanDefinitionParserDelegate parentDelegate) {
		//新建Bean定义解析器装饰器
		BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
		//进行默认初始化
		delegate.initDefaults(root, parentDelegate);
		//返回该装饰器
		return delegate;
	}

	//解析Bean定义
	protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
		if (delegate.isDefaultNamespace(root)) {
			NodeList nl = root.getChildNodes();
			//遍历所有子节点，对各个子节点进行解析
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node instanceof Element) {
					Element ele = (Element) node;
					if (delegate.isDefaultNamespace(ele)) {
						//解析默认名称空间元素
						parseDefaultElement(ele, delegate);
					} else {
						//解析外部名称空间元素
						delegate.parseCustomElement(ele);
					}
				}
			}
		} else {
			//解析外部名称空间元素
			delegate.parseCustomElement(root);
		}
	}

	//解析默认元素
	private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
		//解析<import>
		if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
			importBeanDefinitionResource(ele);
		//解析<alias>
		} else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
			processAliasRegistration(ele);
		//解析<bean>
		} else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
			processBeanDefinition(ele, delegate);
		//解析<beans>
		} else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
			doRegisterBeanDefinitions(ele);
		}
	}

	//解析<import>
	protected void importBeanDefinitionResource(Element ele) {
		//获取resource属性值
		String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
		//若resource属性为空，则记录错误并返回
		if (!StringUtils.hasText(location)) {
			getReaderContext().error("Resource location must not be empty", ele);
			return;
		}

		//解析资源路径中的系统属性
		location = getReaderContext().getEnvironment().resolveRequiredPlaceholders(location);
		//新建已创建资源文件集合
		Set<Resource> actualResources = new LinkedHashSet<Resource>(4);
		//判断是绝对路径还是相对路径
		boolean absoluteLocation = false;
		try {
			absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
		} catch (URISyntaxException ex) {
			//ignore
		}

		//如果是绝对路径
		if (absoluteLocation) {
			try {
				//获取Bean定义阅读器并加载配置文件
				int importCount = getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
				if (logger.isDebugEnabled()) {
					logger.debug("Imported " + importCount + " bean definitions from URL location [" + location + "]");
				}
			} catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to import bean definitions from URL location [" + location + "]", ele,
						ex);
			}
		//如果是相对路径
		} else {
			try {
				int importCount;
				//根据相对路径获取资源
				Resource relativeResource = getReaderContext().getResource().createRelative(location);
				if (relativeResource.exists()) {
					importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
					actualResources.add(relativeResource);
				} else {
					String baseLocation = getReaderContext().getResource().getURL().toString();
					importCount = getReaderContext().getReader().loadBeanDefinitions(
							StringUtils.applyRelativePath(baseLocation, location), actualResources);
				}
				if (logger.isDebugEnabled()) {
					logger.debug(
							"Imported " + importCount + " bean definitions from relative location [" + location + "]");
				}
			} catch (IOException ex) {
				getReaderContext().error("Failed to resolve current resource location", ele, ex);
			} catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to import bean definitions from relative location [" + location + "]",
						ele, ex);
			}
		}
		Resource[] actResArray = actualResources.toArray(new Resource[actualResources.size()]);
		//处理完之后发送事件消息
		getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
	}

	//解析<alias>
	protected void processAliasRegistration(Element ele) {
		String name = ele.getAttribute(NAME_ATTRIBUTE);
		String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
		boolean valid = true;
		//若name属性值为空，则记录异常
		if (!StringUtils.hasText(name)) {
			getReaderContext().error("Name must not be empty", ele);
			valid = false;
		}
		//若alias属性值为空，则记录异常
		if (!StringUtils.hasText(alias)) {
			getReaderContext().error("Alias must not be empty", ele);
			valid = false;
		}

		//若name和alias属性都有值
		if (valid) {
			try {
				//获取Bean定义注册器并注册别名
				getReaderContext().getRegistry().registerAlias(name, alias);
			} catch (Exception ex) {
				getReaderContext().error("Failed to register alias '" + alias + "' for bean with name '" + name + "'",
						ele, ex);
			}
			//注册完之后发送已注册事件
			getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
		}
	}

	//解析<bean>
	protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
		//对节点进行解析，获取Bean定义持有器
		BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
		if (bdHolder != null) {
			//如果有需要则对Bean定义进行修饰
			bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
			try {
				//将Bean定义注册到注册器中
				BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
			} catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to register bean definition with name '" + bdHolder.getBeanName() + "'", ele, ex);
			}
			//注册完之后发送已注册事件
			getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
		}
	}

	//解析XML之前执行
	protected void preProcessXml(Element root) {}

	//解析XML之后执行
	protected void postProcessXml(Element root) {}

}
