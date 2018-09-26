package org.springframework.web.servlet.support;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.util.HtmlUtils;
import java.beans.PropertyEditor;
import java.util.Arrays;
import java.util.List;

//绑定状态
public class BindStatus {

    private final RequestContext requestContext;

    private final String path;

    private final boolean htmlEscape;

    private final String expression;

    private final Errors errors;

    private BindingResult bindingResult;

    private Object value;

    private Class<?> valueType;

    private Object actualValue;

    private PropertyEditor editor;

    private List<? extends ObjectError> objectErrors;

    private String[] errorCodes;

    private String[] errorMessages;

    //构造器
    public BindStatus(RequestContext requestContext, String path, boolean htmlEscape)
            throws IllegalStateException {

        this.requestContext = requestContext;
        this.path = path;
        this.htmlEscape = htmlEscape;

        // determine name of the object and property
        String beanName;
        int dotPos = path.indexOf('.');
        if (dotPos == -1) {
            // property not set, only the object itself
            beanName = path;
            this.expression = null;
        } else {
            beanName = path.substring(0, dotPos);
            this.expression = path.substring(dotPos + 1);
        }

        this.errors = requestContext.getErrors(beanName, false);

        if (this.errors != null) {
            // Usual case: A BindingResult is available as request attribute.
            // Can determine error codes and messages for the given expression.
            // Can use a custom PropertyEditor, as registered by a form controller.
            if (this.expression != null) {
                if ("*".equals(this.expression)) {
                    this.objectErrors = this.errors.getAllErrors();
                } else if (this.expression.endsWith("*")) {
                    this.objectErrors = this.errors.getFieldErrors(this.expression);
                } else {
                    this.objectErrors = this.errors.getFieldErrors(this.expression);
                    this.value = this.errors.getFieldValue(this.expression);
                    this.valueType = this.errors.getFieldType(this.expression);
                    if (this.errors instanceof BindingResult) {
                        this.bindingResult = (BindingResult) this.errors;
                        this.actualValue = this.bindingResult.getRawFieldValue(this.expression);
                        this.editor = this.bindingResult.findEditor(this.expression, null);
                    } else {
                        this.actualValue = this.value;
                    }
                }
            } else {
                this.objectErrors = this.errors.getGlobalErrors();
            }
            initErrorCodes();
        } else {
            // No BindingResult available as request attribute:
            // Probably forwarded directly to a form view.
            // Let's do the best we can: extract a plain target if appropriate.
            Object target = requestContext.getModelObject(beanName);
            if (target == null) {
                throw new IllegalStateException("Neither BindingResult nor plain target object for bean name '" +
                        beanName + "' available as request attribute");
            }
            if (this.expression != null && !"*".equals(this.expression) && !this.expression.endsWith("*")) {
                BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(target);
                this.value = bw.getPropertyValue(this.expression);
                this.valueType = bw.getPropertyType(this.expression);
                this.actualValue = this.value;
            }
            this.errorCodes = new String[0];
            this.errorMessages = new String[0];
        }

        if (htmlEscape && this.value instanceof String) {
            this.value = HtmlUtils.htmlEscape((String) this.value);
        }
    }

    //初始化错误码
    private void initErrorCodes() {
        this.errorCodes = new String[this.objectErrors.size()];
        for (int i = 0; i < this.objectErrors.size(); i++) {
            ObjectError error = this.objectErrors.get(i);
            this.errorCodes[i] = error.getCode();
        }
    }

    //初始化错误消息
    private void initErrorMessages() throws NoSuchMessageException {
        if (this.errorMessages == null) {
            this.errorMessages = new String[this.objectErrors.size()];
            for (int i = 0; i < this.objectErrors.size(); i++) {
                ObjectError error = this.objectErrors.get(i);
                this.errorMessages[i] = this.requestContext.getMessage(error, this.htmlEscape);
            }
        }
    }

    //获取路径
    public String getPath() {
        return this.path;
    }

    //获取表达式
    public String getExpression() {
        return this.expression;
    }

    //获取值
    public Object getValue() {
        return this.value;
    }

    //获取值类型
    public Class<?> getValueType() {
        return this.valueType;
    }

    //获取实际值
    public Object getActualValue() {
        return this.actualValue;
    }

    /**
     * Return a suitable display value for the field, i.e. the stringified
     * value if not null, and an empty string in case of a null value.
     * <p>This value will be an HTML-escaped String if the original value
     * was non-null: the {@code toString} result of the original value
     * will get HTML-escaped.
     */
    public String getDisplayValue() {
        if (this.value instanceof String) {
            return (String) this.value;
        }
        if (this.value != null) {
            return (this.htmlEscape ? HtmlUtils.htmlEscape(this.value.toString()) : this.value.toString());
        }
        return "";
    }

    //是否错误
    public boolean isError() {
        return (this.errorCodes != null && this.errorCodes.length > 0);
    }

    //获取所有错误码
    public String[] getErrorCodes() {
        return this.errorCodes;
    }

    //获取第一个错误码
    public String getErrorCode() {
        return (this.errorCodes.length > 0 ? this.errorCodes[0] : "");
    }

    //获取错误消息
    public String[] getErrorMessages() {
        initErrorMessages();
        return this.errorMessages;
    }

    //获取第一个错误消息
    public String getErrorMessage() {
        initErrorMessages();
        return (this.errorMessages.length > 0 ? this.errorMessages[0] : "");
    }

    //获取错误消息
    public String getErrorMessagesAsString(String delimiter) {
        initErrorMessages();
        return StringUtils.arrayToDelimitedString(this.errorMessages, delimiter);
    }

    //获取错误
    public Errors getErrors() {
        return this.errors;
    }

    //获取属性编辑器
    public PropertyEditor getEditor() {
        return this.editor;
    }

    //寻找属性编辑器
    public PropertyEditor findEditor(Class<?> valueClass) {
        return (this.bindingResult != null ? this.bindingResult.findEditor(this.expression, valueClass) : null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BindStatus: ");
        sb.append("expression=[").append(this.expression).append("]; ");
        sb.append("value=[").append(this.value).append("]");
        if (isError()) {
            sb.append("; errorCodes=").append(Arrays.asList(this.errorCodes));
        }
        return sb.toString();
    }

}
