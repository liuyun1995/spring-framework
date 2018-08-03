package org.springframework.beans;

import java.beans.PropertyEditor;

//属性编辑器注册器
public interface PropertyEditorRegistry {

	//注册外部属性编辑器
	void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor);

	//注册外部属性编辑器
	void registerCustomEditor(Class<?> requiredType, String propertyPath, PropertyEditor propertyEditor);

	//寻找外部属性编辑器
	PropertyEditor findCustomEditor(Class<?> requiredType, String propertyPath);

}
