package org.springframework.web.servlet.theme;

import org.springframework.web.servlet.ThemeResolver;

//抽象主题解析器
public abstract class AbstractThemeResolver implements ThemeResolver {

	public final static String ORIGINAL_DEFAULT_THEME_NAME = "theme";
	private String defaultThemeName = ORIGINAL_DEFAULT_THEME_NAME;

	//设置默认主题名
	public void setDefaultThemeName(String defaultThemeName) {
		this.defaultThemeName = defaultThemeName;
	}

	//获取默认主题名
	public String getDefaultThemeName() {
		return this.defaultThemeName;
	}

}
