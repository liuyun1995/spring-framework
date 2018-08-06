package org.springframework.beans.support;

import java.beans.PropertyEditor;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;

import org.xml.sax.InputSource;

import org.springframework.beans.property.PropertyEditorRegistrar;
import org.springframework.beans.property.PropertyEditorRegistry;
import org.springframework.beans.property.PropertyEditorRegistrySupport;
import org.springframework.beans.property.editors.ClassArrayEditor;
import org.springframework.beans.property.editors.ClassEditor;
import org.springframework.beans.property.editors.FileEditor;
import org.springframework.beans.property.editors.InputSourceEditor;
import org.springframework.beans.property.editors.InputStreamEditor;
import org.springframework.beans.property.editors.PathEditor;
import org.springframework.beans.property.editors.ReaderEditor;
import org.springframework.beans.property.editors.URIEditor;
import org.springframework.beans.property.editors.URLEditor;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourceArrayPropertyEditor;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;

//资源编辑器注册器
public class ResourceEditorRegistrar implements PropertyEditorRegistrar {

    private static Class<?> pathClass;
    private final PropertyResolver propertyResolver;
    private final ResourceLoader resourceLoader;

    static {
        try {
            pathClass = ClassUtils.forName("java.nio.file.Path", ResourceEditorRegistrar.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            pathClass = null;
        }
    }

    public ResourceEditorRegistrar(ResourceLoader resourceLoader, PropertyResolver propertyResolver) {
        this.resourceLoader = resourceLoader;
        this.propertyResolver = propertyResolver;
    }

    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        ResourceEditor baseEditor = new ResourceEditor(this.resourceLoader, this.propertyResolver);
        doRegisterEditor(registry, Resource.class, baseEditor);
        doRegisterEditor(registry, ContextResource.class, baseEditor);
        doRegisterEditor(registry, InputStream.class, new InputStreamEditor(baseEditor));
        doRegisterEditor(registry, InputSource.class, new InputSourceEditor(baseEditor));
        doRegisterEditor(registry, File.class, new FileEditor(baseEditor));
        if (pathClass != null) {
            doRegisterEditor(registry, pathClass, new PathEditor(baseEditor));
        }
        doRegisterEditor(registry, Reader.class, new ReaderEditor(baseEditor));
        doRegisterEditor(registry, URL.class, new URLEditor(baseEditor));

        ClassLoader classLoader = this.resourceLoader.getClassLoader();
        doRegisterEditor(registry, URI.class, new URIEditor(classLoader));
        doRegisterEditor(registry, Class.class, new ClassEditor(classLoader));
        doRegisterEditor(registry, Class[].class, new ClassArrayEditor(classLoader));

        if (this.resourceLoader instanceof ResourcePatternResolver) {
            doRegisterEditor(registry, Resource[].class,
                    new ResourceArrayPropertyEditor((ResourcePatternResolver) this.resourceLoader, this.propertyResolver));
        }
    }

    private void doRegisterEditor(PropertyEditorRegistry registry, Class<?> requiredType, PropertyEditor editor) {
        if (registry instanceof PropertyEditorRegistrySupport) {
            ((PropertyEditorRegistrySupport) registry).overrideDefaultEditor(requiredType, editor);
        } else {
            registry.registerCustomEditor(requiredType, editor);
        }
    }

}
