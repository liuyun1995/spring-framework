package org.springframework.beans.factory.config;

import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.springframework.beans.exception.BeanDefinitionStoreException;
import org.springframework.beans.bean.InitializingBean;

public class PreferencesPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean {

    private String systemTreePath;
    private String userTreePath;
    private Preferences systemPrefs;
    private Preferences userPrefs;

    public void setSystemTreePath(String systemTreePath) {
        this.systemTreePath = systemTreePath;
    }

    public void setUserTreePath(String userTreePath) {
        this.userTreePath = userTreePath;
    }

    @Override
    public void afterPropertiesSet() {
        this.systemPrefs = (this.systemTreePath != null) ?
                Preferences.systemRoot().node(this.systemTreePath) : Preferences.systemRoot();
        this.userPrefs = (this.userTreePath != null) ?
                Preferences.userRoot().node(this.userTreePath) : Preferences.userRoot();
    }

    @Override
    protected String resolvePlaceholder(String placeholder, Properties props) {
        String path = null;
        String key = placeholder;
        int endOfPath = placeholder.lastIndexOf('/');
        if (endOfPath != -1) {
            path = placeholder.substring(0, endOfPath);
            key = placeholder.substring(endOfPath + 1);
        }
        String value = resolvePlaceholder(path, key, this.userPrefs);
        if (value == null) {
            value = resolvePlaceholder(path, key, this.systemPrefs);
            if (value == null) {
                value = props.getProperty(placeholder);
            }
        }
        return value;
    }

    protected String resolvePlaceholder(String path, String key, Preferences preferences) {
        if (path != null) {
            // Do not create the node if it does not exist...
            try {
                if (preferences.nodeExists(path)) {
                    return preferences.node(path).get(key, null);
                } else {
                    return null;
                }
            } catch (BackingStoreException ex) {
                throw new BeanDefinitionStoreException("Cannot access specified node path [" + path + "]", ex);
            }
        } else {
            return preferences.get(key, null);
        }
    }

}
