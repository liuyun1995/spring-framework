package org.springframework.beans.factory.config.factorybean;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.beans.factory.config.factorybean.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class YamlMapFactoryBean extends YamlProcessor implements FactoryBean<Map<String, Object>>, InitializingBean {

    private boolean singleton = true;

    private Map<String, Object> map;

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    @Override
    public boolean isSingleton() {
        return this.singleton;
    }

    @Override
    public void afterPropertiesSet() {
        if (isSingleton()) {
            this.map = createMap();
        }
    }

    @Override
    public Map<String, Object> getObject() {
        return (this.map != null ? this.map : createMap());
    }

    @Override
    public Class<?> getObjectType() {
        return Map.class;
    }

    protected Map<String, Object> createMap() {
        final Map<String, Object> result = new LinkedHashMap<String, Object>();
        process(new MatchCallback() {
            @Override
            public void process(Properties properties, Map<String, Object> map) {
                merge(result, map);
            }
        });
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void merge(Map<String, Object> output, Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Object existing = output.get(key);
            if (value instanceof Map && existing instanceof Map) {
                Map<String, Object> result = new LinkedHashMap<String, Object>((Map) existing);
                merge(result, (Map) value);
                output.put(key, result);
            } else {
                output.put(key, value);
            }
        }
    }

}
