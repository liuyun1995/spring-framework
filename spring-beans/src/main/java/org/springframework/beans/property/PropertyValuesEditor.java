package org.springframework.beans.property;

import org.springframework.beans.property.editors.PropertiesEditor;
import java.beans.PropertyEditorSupport;
import java.util.Properties;

//属性值编辑器
public class PropertyValuesEditor extends PropertyEditorSupport {

	//属性编辑器
	private final PropertiesEditor propertiesEditor = new PropertiesEditor();

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		this.propertiesEditor.setAsText(text);
		Properties props = (Properties) this.propertiesEditor.getValue();
		setValue(new MutablePropertyValues(props));
	}

}

