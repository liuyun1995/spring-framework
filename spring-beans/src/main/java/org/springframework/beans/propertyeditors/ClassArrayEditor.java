package org.springframework.beans.propertyeditors;

import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;

public class ClassArrayEditor extends PropertyEditorSupport {

    private final ClassLoader classLoader;

    public ClassArrayEditor() {
        this(null);
    }

    public ClassArrayEditor(ClassLoader classLoader) {
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            String[] classNames = StringUtils.commaDelimitedListToStringArray(text);
            Class<?>[] classes = new Class<?>[classNames.length];
            for (int i = 0; i < classNames.length; i++) {
                String className = classNames[i].trim();
                classes[i] = ClassUtils.resolveClassName(className, this.classLoader);
            }
            setValue(classes);
        } else {
            setValue(null);
        }
    }

    @Override
    public String getAsText() {
        Class<?>[] classes = (Class[]) getValue();
        if (ObjectUtils.isEmpty(classes)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < classes.length; ++i) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(ClassUtils.getQualifiedName(classes[i]));
        }
        return sb.toString();
    }

}
