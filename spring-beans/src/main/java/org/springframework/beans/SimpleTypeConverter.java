package org.springframework.beans;

//简单类型转换器
public class SimpleTypeConverter extends TypeConverterSupport {

	//构造器
	public SimpleTypeConverter() {
		//新建类型转换器助手
		this.typeConverterDelegate = new TypeConverterDelegate(this);
		//激活默认编辑器
		registerDefaultEditors();
	}

}
