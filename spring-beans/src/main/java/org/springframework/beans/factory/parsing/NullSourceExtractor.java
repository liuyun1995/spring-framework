package org.springframework.beans.factory.parsing;

import org.springframework.core.io.Resource;

public class NullSourceExtractor implements SourceExtractor {

	@Override
	public Object extractSource(Object sourceCandidate, Resource definitionResource) {
		return null;
	}

}
