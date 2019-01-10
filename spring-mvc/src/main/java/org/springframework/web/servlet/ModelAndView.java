package org.springframework.web.servlet;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;

//视图模型
public class ModelAndView {

    //视图对象
    private Object view;
    //数据模型
    private ModelMap model;
    //HTTP状态
    private HttpStatus status;
    //是否清空过
    private boolean cleared = false;

    //构造器1
    public ModelAndView() {}

    //构造器2
    public ModelAndView(String viewName) {
        this.view = viewName;
    }

    //构造器3
    public ModelAndView(View view) {
        this.view = view;
    }

    //构造器3
    public ModelAndView(String viewName, Map<String, ?> model) {
        this.view = viewName;
        if (model != null) {
            getModelMap().addAllAttributes(model);
        }
    }

    //构造器4
    public ModelAndView(View view, Map<String, ?> model) {
        this.view = view;
        if (model != null) {
            getModelMap().addAllAttributes(model);
        }
    }

    //构造器5
    public ModelAndView(String viewName, HttpStatus status) {
        this.view = viewName;
        this.status = status;
    }

    //构造器6
    public ModelAndView(String viewName, Map<String, ?> model, HttpStatus status) {
        this.view = viewName;
        if (model != null) {
            getModelMap().addAllAttributes(model);
        }
        this.status = status;
    }

    //构造器7
    public ModelAndView(String viewName, String modelName, Object modelObject) {
        this.view = viewName;
        addObject(modelName, modelObject);
    }

    //构造器8
    public ModelAndView(View view, String modelName, Object modelObject) {
        this.view = view;
        addObject(modelName, modelObject);
    }

    //设置视图名称
    public void setViewName(String viewName) {
        this.view = viewName;
    }

    //获取视图名称
    public String getViewName() {
        return (this.view instanceof String ? (String) this.view : null);
    }

    //设置视图
    public void setView(View view) {
        this.view = view;
    }

    //获取视图
    public View getView() {
        return (this.view instanceof View ? (View) this.view : null);
    }

    //是否存在视图
    public boolean hasView() {
        return (this.view != null);
    }

    //是否使用视图引用
    public boolean isReference() {
        return (this.view instanceof String);
    }

    //获取数据模型
    protected Map<String, Object> getModelInternal() {
        return this.model;
    }

    //获取数据模型
    public ModelMap getModelMap() {
        if (this.model == null) {
            this.model = new ModelMap();
        }
        return this.model;
    }

    //获取数据模型
    public Map<String, Object> getModel() {
        return getModelMap();
    }

    //设置响应状态
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    //设置响应状态
    public HttpStatus getStatus() {
        return this.status;
    }

    //添加键值对
    public ModelAndView addObject(String attributeName, Object attributeValue) {
        getModelMap().addAttribute(attributeName, attributeValue);
        return this;
    }

    //添加键值对
    public ModelAndView addObject(Object attributeValue) {
        getModelMap().addAttribute(attributeValue);
        return this;
    }

    //添加键值对
    public ModelAndView addAllObjects(Map<String, ?> modelMap) {
        getModelMap().addAllAttributes(modelMap);
        return this;
    }

    //清空方法
    public void clear() {
        this.view = null;
        this.model = null;
        this.cleared = true;
    }

    //是否为空
    public boolean isEmpty() {
        return (this.view == null && CollectionUtils.isEmpty(this.model));
    }

    //是否清空过
    public boolean wasCleared() {
        return (this.cleared && isEmpty());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ModelAndView: ");
        if (isReference()) {
            sb.append("reference to view with name '").append(this.view).append("'");
        } else {
            sb.append("materialized View is [").append(this.view).append(']');
        }
        sb.append("; model is ").append(this.model);
        return sb.toString();
    }

}
