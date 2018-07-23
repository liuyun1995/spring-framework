package org.springframework.beans.factory.parsing;

import java.util.LinkedList;
import java.util.List;

import org.springframework.util.Assert;

//混合组件定义
public class CompositeComponentDefinition extends AbstractComponentDefinition {

	private final String name;

	private final Object source;

	private final List<ComponentDefinition> nestedComponents = new LinkedList<ComponentDefinition>();

	public CompositeComponentDefinition(String name, Object source) {
		Assert.notNull(name, "Name must not be null");
		this.name = name;
		this.source = source;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Object getSource() {
		return this.source;
	}

	public void addNestedComponent(ComponentDefinition component) {
		Assert.notNull(component, "ComponentDefinition must not be null");
		this.nestedComponents.add(component);
	}

	public ComponentDefinition[] getNestedComponents() {
		return this.nestedComponents.toArray(new ComponentDefinition[this.nestedComponents.size()]);
	}

}
