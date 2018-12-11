package org.springframework.web.servlet.mvc.condition;

import java.util.Collection;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;

//请求表达式持有器
public final class RequestConditionHolder extends AbstractRequestCondition<RequestConditionHolder> {

    private final RequestCondition<Object> condition;

    //构造器
    @SuppressWarnings("unchecked")
    public RequestConditionHolder(RequestCondition<?> requestCondition) {
        this.condition = (RequestCondition<Object>) requestCondition;
    }

    //获取表达式
    public RequestCondition<?> getCondition() {
        return this.condition;
    }

    @Override
    protected Collection<?> getContent() {
        return (this.condition != null ? Collections.singleton(this.condition) : Collections.emptyList());
    }

    @Override
    protected String getToStringInfix() {
        return " ";
    }

    //联合方法
    @Override
    public RequestConditionHolder combine(RequestConditionHolder other) {
        if (this.condition == null && other.condition == null) {
            return this;
        } else if (this.condition == null) {
            return other;
        } else if (other.condition == null) {
            return this;
        } else {
            assertEqualConditionTypes(other);
            RequestCondition<?> combined = (RequestCondition<?>) this.condition.combine(other.condition);
            return new RequestConditionHolder(combined);
        }
    }

    /**
     * Ensure the held request conditions are of the same type.
     */
    private void assertEqualConditionTypes(RequestConditionHolder other) {
        Class<?> clazz = this.condition.getClass();
        Class<?> otherClazz = other.condition.getClass();
        if (!clazz.equals(otherClazz)) {
            throw new ClassCastException("Incompatible request conditions: " + clazz + " and " + otherClazz);
        }
    }

    //获取匹配的表达式
    @Override
    public RequestConditionHolder getMatchingCondition(HttpServletRequest request) {
        if (this.condition == null) {
            return this;
        }
        RequestCondition<?> match = (RequestCondition<?>) this.condition.getMatchingCondition(request);
        return (match != null ? new RequestConditionHolder(match) : null);
    }

    //比较方法
    @Override
    public int compareTo(RequestConditionHolder other, HttpServletRequest request) {
        if (this.condition == null && other.condition == null) {
            return 0;
        } else if (this.condition == null) {
            return 1;
        } else if (other.condition == null) {
            return -1;
        } else {
            assertEqualConditionTypes(other);
            return this.condition.compareTo(other.condition, request);
        }
    }

}
