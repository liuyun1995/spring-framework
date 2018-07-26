package org.springframework.beans.factory.parsing;

import org.springframework.core.io.Resource;

//资源提取器
public interface SourceExtractor {

	Object extractSource(Object sourceCandidate, Resource definingResource);

}
