package org.springframework.beans.factory.parsing;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class Location {

	private final Resource resource;

	private final Object source;

	public Location(Resource resource) {
		this(resource, null);
	}

	public Location(Resource resource, Object source) {
		Assert.notNull(resource, "Resource must not be null");
		this.resource = resource;
		this.source = source;
	}

	public Resource getResource() {
		return this.resource;
	}

	public Object getSource() {
		return this.source;
	}

}
