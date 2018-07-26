package org.springframework.beans.factory.parsing;

import java.util.EventListener;

//解析事件监听器
public interface ReaderEventListener extends EventListener {

	//默认定义注册完毕
	void defaultsRegistered(DefaultsDefinition defaultsDefinition);

	//组件定义注册完毕
	void componentRegistered(ComponentDefinition componentDefinition);

	//别名定义注册完毕
	void aliasRegistered(AliasDefinition aliasDefinition);

	//导入定义注册完毕
	void importProcessed(ImportDefinition importDefinition);

}
