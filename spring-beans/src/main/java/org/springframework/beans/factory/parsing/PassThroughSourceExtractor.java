package org.springframework.beans.factory.parsing;

import org.springframework.core.io.Resource;

public class PassThroughSourceExtractor implements SourceExtractor {

	@Override
	public Object extractSource(Object sourceCandidate, Resource definingResource) {
		return sourceCandidate;
	}

}
