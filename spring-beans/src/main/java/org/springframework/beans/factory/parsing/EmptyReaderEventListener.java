package org.springframework.beans.factory.parsing;

//空的解析事件监听器
public class EmptyReaderEventListener implements ReaderEventListener {

	@Override
	public void defaultsRegistered(DefaultsDefinition defaultsDefinition) {
		// no-op
	}

	@Override
	public void componentRegistered(ComponentDefinition componentDefinition) {
		// no-op
	}

	@Override
	public void aliasRegistered(AliasDefinition aliasDefinition) {
		// no-op
	}

	@Override
	public void importProcessed(ImportDefinition importDefinition) {
		// no-op
	}

}
