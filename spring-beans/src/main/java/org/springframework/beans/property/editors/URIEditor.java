package org.springframework.beans.property.editors;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

public class URIEditor extends PropertyEditorSupport {

	private final ClassLoader classLoader;

	private final boolean encode;

	public URIEditor() {
		this(true);
	}

	public URIEditor(boolean encode) {
		this.classLoader = null;
		this.encode = encode;
	}

	public URIEditor(ClassLoader classLoader) {
		this(classLoader, true);
	}

	public URIEditor(ClassLoader classLoader, boolean encode) {
		this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
		this.encode = encode;
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			String uri = text.trim();
			if (this.classLoader != null && uri.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
				ClassPathResource resource = new ClassPathResource(
						uri.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length()), this.classLoader);
				try {
					String url = resource.getURL().toString();
					setValue(createURI(url));
				} catch (IOException ex) {
					throw new IllegalArgumentException(
							"Could not retrieve URI for " + resource + ": " + ex.getMessage());
				} catch (URISyntaxException ex) {
					throw new IllegalArgumentException("Invalid URI syntax: " + ex);
				}
			} else {
				try {
					setValue(createURI(uri));
				} catch (URISyntaxException ex) {
					throw new IllegalArgumentException("Invalid URI syntax: " + ex);
				}
			}
		} else {
			setValue(null);
		}
	}
	
	protected URI createURI(String value) throws URISyntaxException {
		int colonIndex = value.indexOf(':');
		if (this.encode && colonIndex != -1) {
			int fragmentIndex = value.indexOf('#', colonIndex + 1);
			String scheme = value.substring(0, colonIndex);
			String ssp = value.substring(colonIndex + 1, (fragmentIndex > 0 ? fragmentIndex : value.length()));
			String fragment = (fragmentIndex > 0 ? value.substring(fragmentIndex + 1) : null);
			return new URI(scheme, ssp, fragment);
		} else {
			// not encoding or the value contains no scheme - fallback to default
			return new URI(value);
		}
	}

	@Override
	public String getAsText() {
		URI value = (URI) getValue();
		return (value != null ? value.toString() : "");
	}

}
