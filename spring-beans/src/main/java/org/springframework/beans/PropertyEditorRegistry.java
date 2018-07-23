package org.springframework.beans;

import java.beans.PropertyEditor;

//属性编辑器注册器
public interface PropertyEditorRegistry {
	
	void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor);
	
	void registerCustomEditor(Class<?> requiredType, String propertyPath, PropertyEditor propertyEditor);
	
	PropertyEditor findCustomEditor(Class<?> requiredType, String propertyPath);

}
