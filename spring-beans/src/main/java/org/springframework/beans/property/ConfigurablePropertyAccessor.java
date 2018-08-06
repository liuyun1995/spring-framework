package org.springframework.beans.property;

import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.property.type.TypeConverter;
import org.springframework.core.convert.ConversionService;

//可配置的属性获取器
public interface ConfigurablePropertyAccessor extends PropertyAccessor, PropertyEditorRegistry, TypeConverter {

	//设置版本服务
	void setConversionService(ConversionService conversionService);

	//获取版本服务
	ConversionService getConversionService();

	void setExtractOldValueForEditor(boolean extractOldValueForEditor);

	boolean isExtractOldValueForEditor();

	void setAutoGrowNestedPaths(boolean autoGrowNestedPaths);

	boolean isAutoGrowNestedPaths();

}
