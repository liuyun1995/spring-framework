package org.springframework.beans.property;

//属性编辑器登记员
public interface PropertyEditorRegistrar {

	//通过注册器来注册属性编辑器
	void registerCustomEditors(PropertyEditorRegistry registry);

}
