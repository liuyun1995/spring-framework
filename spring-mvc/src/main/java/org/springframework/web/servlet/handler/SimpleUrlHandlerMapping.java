package org.springframework.web.servlet.handler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.springframework.beans.BeansException;
import org.springframework.util.CollectionUtils;

//简单URL处理器映射
public class SimpleUrlHandlerMapping extends AbstractUrlHandlerMapping {

    private final Map<String, Object> urlMap = new LinkedHashMap<String, Object>();

    //设置属性映射
    public void setMappings(Properties mappings) {
        CollectionUtils.mergePropertiesIntoMap(mappings, this.urlMap);
    }

    //设置URL映射
    public void setUrlMap(Map<String, ?> urlMap) {
        this.urlMap.putAll(urlMap);
    }

    //获取URL映射
    public Map<String, ?> getUrlMap() {
        return this.urlMap;
    }

    //初始化应用上下文
    @Override
    public void initApplicationContext() throws BeansException {
        super.initApplicationContext();
        registerHandlers(this.urlMap);
    }

    //注册处理器
    protected void registerHandlers(Map<String, Object> urlMap) throws BeansException {
        if (urlMap.isEmpty()) {
            logger.warn("Neither 'urlMap' nor 'mappings' set on SimpleUrlHandlerMapping");
        } else {
            for (Map.Entry<String, Object> entry : urlMap.entrySet()) {
                String url = entry.getKey();
                Object handler = entry.getValue();
                // Prepend with slash if not already present.
                if (!url.startsWith("/")) {
                    url = "/" + url;
                }
                // Remove whitespace from handler bean name.
                if (handler instanceof String) {
                    handler = ((String) handler).trim();
                }
                registerHandler(url, handler);
            }
        }
    }

}
