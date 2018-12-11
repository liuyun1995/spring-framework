package org.springframework.web.servlet.resource;

import org.springframework.core.io.Resource;

//固定的版本策略器
public class FixedVersionStrategy extends AbstractVersionStrategy {

	private final String version;

	public FixedVersionStrategy(String version) {
		super(new PrefixVersionPathStrategy(version));
		this.version = version;
	}

	@Override
	public String getResourceVersion(Resource resource) {
		return this.version;
	}

}
