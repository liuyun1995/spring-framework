package org.springframework.web.servlet.mvc.condition;

import javax.servlet.http.HttpServletRequest;

/**
 * Supports "name=value" style expressions as described in:
 * {@link org.springframework.web.bind.annotation.RequestMapping#params()} and
 * {@link org.springframework.web.bind.annotation.RequestMapping#headers()}.
 *
 * @author Rossen Stoyanchev
 * @author Arjen Poutsma
 * @since 3.1
 */
abstract class AbstractNameValueExpression<T> implements NameValueExpression<T> {

    protected final String name;

    protected final T value;

    protected final boolean isNegated;


    AbstractNameValueExpression(String expression) {
        int separator = expression.indexOf('=');
        if (separator == -1) {
            this.isNegated = expression.startsWith("!");
            this.name = (this.isNegated ? expression.substring(1) : expression);
            this.value = null;
        } else {
            this.isNegated = (separator > 0) && (expression.charAt(separator - 1) == '!');
            this.name = (this.isNegated ? expression.substring(0, separator - 1) : expression.substring(0, separator));
            this.value = parseValue(expression.substring(separator + 1));
        }
    }


    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public T getValue() {
        return this.value;
    }

    @Override
    public boolean isNegated() {
        return this.isNegated;
    }

    public final boolean match(HttpServletRequest request) {
        boolean isMatch;
        if (this.value != null) {
            isMatch = matchValue(request);
        } else {
            isMatch = matchName(request);
        }
        return (this.isNegated ? !isMatch : isMatch);
    }


    protected abstract boolean isCaseSensitiveName();

    protected abstract T parseValue(String valueExpression);

    protected abstract boolean matchName(HttpServletRequest request);

    protected abstract boolean matchValue(HttpServletRequest request);


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AbstractNameValueExpression) {
            AbstractNameValueExpression<?> other = (AbstractNameValueExpression<?>) obj;
            String thisName = (isCaseSensitiveName() ? this.name : this.name.toLowerCase());
            String otherName = (isCaseSensitiveName() ? other.name : other.name.toLowerCase());
            return (thisName.equalsIgnoreCase(otherName) &&
                    (this.value != null ? this.value.equals(other.value) : other.value == null) &&
                    this.isNegated == other.isNegated);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = (isCaseSensitiveName() ? this.name.hashCode() : this.name.toLowerCase().hashCode());
        result = 31 * result + (this.value != null ? this.value.hashCode() : 0);
        result = 31 * result + (this.isNegated ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.value != null) {
            builder.append(this.name);
            if (this.isNegated) {
                builder.append('!');
            }
            builder.append('=');
            builder.append(this.value);
        } else {
            if (this.isNegated) {
                builder.append('!');
            }
            builder.append(this.name);
        }
        return builder.toString();
    }

}
