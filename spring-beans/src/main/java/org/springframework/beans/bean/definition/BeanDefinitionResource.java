package org.springframework.beans.bean.definition;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.AbstractResource;
import org.springframework.util.Assert;

//Bean定义资源
class BeanDefinitionResource extends AbstractResource {

	private final BeanDefinition beanDefinition;
	
	public BeanDefinitionResource(BeanDefinition beanDefinition) {
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		this.beanDefinition = beanDefinition;
	}
	
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
