package org.springframework.beans.bean.definition;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.AbstractResource;
import org.springframework.util.Assert;

//Bean定义资源
class BeanDefinitionResource extends AbstractResource {

	//Bean定义
	private final BeanDefinition beanDefinition;

	//构造器
	public BeanDefinitionResource(BeanDefinition beanDefinition) {
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		this.beanDefinition = beanDefinition;
	}

	//获取Bean定义
	public final BeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}
	
	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public boolean isReadable() {
		return false;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		throw new FileNotFoundException(
				"Resource cannot be opened because it points to " + getDescription());
	}

	@Override
	public String getDescription() {
		return "BeanDefinition defined in " + this.beanDefinition.getResourceDescription();
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj == this ||
			(obj instanceof BeanDefinitionResource &&
						((BeanDefinitionResource) obj).beanDefinition.equals(this.beanDefinition)));
	}
	
	@Override
	public int hashCode() {
		return this.beanDefinition.hashCode();
	}

}
