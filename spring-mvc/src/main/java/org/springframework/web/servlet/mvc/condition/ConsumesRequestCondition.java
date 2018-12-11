package org.springframework.web.servlet.mvc.condition;

import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition.HeaderExpression;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * A logical disjunction (' || ') request condition to match a request's
 * 'Content-Type' header to a list of media type expressions. Two kinds of
 * media type expressions are supported, which are described in
 * {@link RequestMapping#consumes()} and {@link RequestMapping#headers()}
 * where the header name is 'Content-Type'. Regardless of which syntax is
 * used, the semantics are the same.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public final class ConsumesRequestCondition extends AbstractRequestCondition<ConsumesRequestCondition> {

    private final static ConsumesRequestCondition PRE_FLIGHT_MATCH = new ConsumesRequestCondition();

    private final List<ConsumeMediaTypeExpression> expressions;

    //构造器
    public ConsumesRequestCondition(String... consumes) {
        this(consumes, null);
    }

    //构造器
    public ConsumesRequestCondition(String[] consumes, String[] headers) {
        this(parseExpressions(consumes, headers));
    }

    //构造器
    private ConsumesRequestCondition(Collection<ConsumeMediaTypeExpression> expressions) {
        this.expressions = new ArrayList<ConsumeMediaTypeExpression>(expressions);
        Collections.sort(this.expressions);
    }

    //解析表达式
    private static Set<ConsumeMediaTypeExpression> parseExpressions(String[] consumes, String[] headers) {
        Set<ConsumeMediaTypeExpression> result = new LinkedHashSet<ConsumeMediaTypeExpression>();
        if (headers != null) {
            for (String header : headers) {
                HeaderExpression expr = new HeaderExpression(header);
                if ("Content-Type".equalsIgnoreCase(expr.name)) {
                    for (MediaType mediaType : MediaType.parseMediaTypes(expr.value)) {
                        result.add(new ConsumeMediaTypeExpression(mediaType, expr.isNegated));
                    }
                }
            }
        }
        if (consumes != null) {
            for (String consume : consumes) {
                result.add(new ConsumeMediaTypeExpression(consume));
            }
        }
        return result;
    }

    //获取表达式
    public Set<MediaTypeExpression> getExpressions() {
        return new LinkedHashSet<MediaTypeExpression>(this.expressions);
    }

    /**
     * Returns the media types for this condition excluding negated expressions.
     */
    public Set<MediaType> getConsumableMediaTypes() {
        Set<MediaType> result = new LinkedHashSet<MediaType>();
        for (ConsumeMediaTypeExpression expression : this.expressions) {
            if (!expression.isNegated()) {
                result.add(expression.getMediaType());
            }
        }
        return result;
    }

    //是否为空
    public boolean isEmpty() {
        return this.expressions.isEmpty();
    }

    @Override
    protected Collection<ConsumeMediaTypeExpression> getContent() {
        return this.expressions;
    }

    @Override
    protected String getToStringInfix() {
        return " || ";
    }

    /**
     * Returns the "other" instance if it has any expressions; returns "this"
     * instance otherwise. Practically that means a method-level "consumes"
     * overrides a type-level "consumes" condition.
     */
    @Override
    public ConsumesRequestCondition combine(ConsumesRequestCondition other) {
        return !other.expressions.isEmpty() ? other : this;
    }

    /**
     * Checks if any of the contained media type expressions match the given
     * request 'Content-Type' header and returns an instance that is guaranteed
     * to contain matching expressions only. The match is performed via
     * {@link MediaType#includes(MediaType)}.
     *
     * @param request the current request
     * @return the same instance if the condition contains no expressions;
     * or a new condition with matching expressions only;
     * or {@code null} if no expressions match.
     */
    @Override
    public ConsumesRequestCondition getMatchingCondition(HttpServletRequest request) {
        if (CorsUtils.isPreFlightRequest(request)) {
            return PRE_FLIGHT_MATCH;
        }
        if (isEmpty()) {
            return this;
        }
        MediaType contentType;
        try {
            contentType = StringUtils.hasLength(request.getContentType()) ?
                    MediaType.parseMediaType(request.getContentType()) :
                    MediaType.APPLICATION_OCTET_STREAM;
        } catch (InvalidMediaTypeException ex) {
            return null;
        }
        Set<ConsumeMediaTypeExpression> result = new LinkedHashSet<ConsumeMediaTypeExpression>(this.expressions);
        for (Iterator<ConsumeMediaTypeExpression> iterator = result.iterator(); iterator.hasNext(); ) {
            ConsumeMediaTypeExpression expression = iterator.next();
            if (!expression.match(contentType)) {
                iterator.remove();
            }
        }
        return (result.isEmpty()) ? null : new ConsumesRequestCondition(result);
    }

    /**
     * Returns:
     * <ul>
     * <li>0 if the two conditions have the same number of expressions
     * <li>Less than 0 if "this" has more or more specific media type expressions
     * <li>Greater than 0 if "other" has more or more specific media type expressions
     * </ul>
     * <p>It is assumed that both instances have been obtained via
     * {@link #getMatchingCondition(HttpServletRequest)} and each instance contains
     * the matching consumable media type expression only or is otherwise empty.
     */
    @Override
    public int compareTo(ConsumesRequestCondition other, HttpServletRequest request) {
        if (this.expressions.isEmpty() && other.expressions.isEmpty()) {
            return 0;
        } else if (this.expressions.isEmpty()) {
            return 1;
        } else if (other.expressions.isEmpty()) {
            return -1;
        } else {
            return this.expressions.get(0).compareTo(other.expressions.get(0));
        }
    }


    /**
     * Parses and matches a single media type expression to a request's 'Content-Type' header.
     */
    static class ConsumeMediaTypeExpression extends AbstractMediaTypeExpression {

        ConsumeMediaTypeExpression(String expression) {
            super(expression);
        }

        ConsumeMediaTypeExpression(MediaType mediaType, boolean negated) {
            super(mediaType, negated);
        }

        public final boolean match(MediaType contentType) {
            boolean match = getMediaType().includes(contentType);
            return (!isNegated() ? match : !match);
        }
    }

}
