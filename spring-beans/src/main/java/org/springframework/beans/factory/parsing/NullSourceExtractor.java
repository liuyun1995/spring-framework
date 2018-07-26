package org.springframework.beans.factory.parsing;

import org.springframework.core.io.Resource;

//空资源提取器
public class NullSourceExtractor implements SourceExtractor {

	@Override
	public Object extractSource(Object sourceCandidate, Resource definitionResource) {
		return null;
	}

}
