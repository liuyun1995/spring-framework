package org.springframework.beans;

//属性编辑器登记员
public interface PropertyEditorRegistrar {

	//注册编辑器
	void registerCustomEditors(PropertyEditorRegistry registry);

}
