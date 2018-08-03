package org.springframework.beans.property.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.TimeZone;

import org.springframework.util.StringUtils;

public class TimeZoneEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		setValue(StringUtils.parseTimeZoneString(text));
	}

	@Override
	public String getAsText() {
		TimeZone value = (TimeZone) getValue();
		return (value != null ? value.getID() : "");
	}

}
