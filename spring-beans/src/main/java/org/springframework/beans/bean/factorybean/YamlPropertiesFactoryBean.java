package org.springframework.beans.bean.factorybean;

import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.beans.bean.InitializingBean;
import org.springframework.core.CollectionFactory;

//Yaml属性工厂Bean
public class YamlPropertiesFactoryBean extends YamlProcessor implements FactoryBean<Properties>, InitializingBean {

	private boolean singleton = true;

	private Properties properties;

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	@Override
	public boolean isSingleton() {
		return this.singleton;
	}

	@Override
	public void afterPropertiesSet() {
		if (isSingleton()) {
			this.properties = createProperties();
		}
	}

	@Override
	public Properties getObject() {
		return (this.properties != null ? this.properties : createProperties());
	}

	@Override
	public Class<?> getObjectType() {
		return Properties.class;
	}

	protected Properties createProperties() {
		final Properties result = CollectionFactory.createStringAdaptingProperties();
		process(new MatchCallback() {
			@Override
			public void process(Properties properties, Map<String, Object> map) {
				result.putAll(properties);
			}
		});
		return result;
	}

}
