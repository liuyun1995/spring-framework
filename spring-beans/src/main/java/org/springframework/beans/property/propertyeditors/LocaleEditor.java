package org.springframework.beans.property.propertyeditors;

import java.beans.PropertyEditorSupport;
import org.springframework.util.StringUtils;

public class LocaleEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) {
		setValue(StringUtils.parseLocaleString(text));
	}

	@Override
	public String getAsText() {
		Object value = getValue();
		return (value != null ? value.toString() : "");
	}

}
