package org.springframework.web.servlet.mvc.condition;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.cors.CorsUtils;

/**
 * A logical conjunction (' && ') request condition that matches a request against
 * a set of header expressions with syntax defined in {@link RequestMapping#headers()}.
 *
 * <p>Expressions passed to the constructor with header names 'Accept' or
 * 'Content-Type' are ignored. See {@link ConsumesRequestCondition} and
 * {@link ProducesRequestCondition} for those.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public final class HeadersRequestCondition extends AbstractRequestCondition<HeadersRequestCondition> {

	private final static HeadersRequestCondition PRE_FLIGHT_MATCH = new HeadersRequestCondition();

	private final Set<HeaderExpression> expressions;

	//构造器
	public HeadersRequestCondition(String... headers) {
		this(parseExpressions(headers));
	}

	//构造器
	private HeadersRequestCondition(Collection<HeaderExpression> conditions) {
		this.expressions = Collections.unmodifiableSet(new LinkedHashSet<HeaderExpression>(conditions));
	}

	//解析表达式
	private static Collection<HeaderExpression> parseExpressions(String... headers) {
		Set<HeaderExpression> expressions = new LinkedHashSet<HeaderExpression>();
		if (headers != null) {
			for (String header : headers) {
				HeaderExpression expr = new HeaderExpression(header);
				if ("Accept".equalsIgnoreCase(expr.name) || "Content-Type".equalsIgnoreCase(expr.name)) {
					continue;
				}
				expressions.add(expr);
			}
		}
		return expressions;
	}

	//获取表达式
	public Set<NameValueExpression<String>> getExpressions() {
		return new LinkedHashSet<NameValueExpression<String>>(this.expressions);
	}

	@Override
	protected Collection<HeaderExpression> getContent() {
		return this.expressions;
	}

	@Override
	protected String getToStringInfix() {
		return " && ";
	}

	/**
	 * Returns a new instance with the union of the header expressions
	 * from "this" and the "other" instance.
	 */
	@Override
	public HeadersRequestCondition combine(HeadersRequestCondition other) {
		Set<HeaderExpression> set = new LinkedHashSet<HeaderExpression>(this.expressions);
		set.addAll(other.expressions);
		return new HeadersRequestCondition(set);
	}

	/**
	 * Returns "this" instance if the request matches all expressions;
	 * or {@code null} otherwise.
	 */
	@Override
	public HeadersRequestCondition getMatchingCondition(HttpServletRequest request) {
		if (CorsUtils.isPreFlightRequest(request)) {
			return PRE_FLIGHT_MATCH;
		}
		for (HeaderExpression expression : expressions) {
			if (!expression.match(request)) {
				return null;
			}
		}
		return this;
	}

	/**
	 * Returns:
	 * <ul>
	 * <li>0 if the two conditions have the same number of header expressions
	 * <li>Less than 0 if "this" instance has more header expressions
	 * <li>Greater than 0 if the "other" instance has more header expressions
	 * </ul>
	 * <p>It is assumed that both instances have been obtained via
	 * {@link #getMatchingCondition(HttpServletRequest)} and each instance
	 * contains the matching header expression only or is otherwise empty.
	 */
	@Override
	public int compareTo(HeadersRequestCondition other, HttpServletRequest request) {
		return other.expressions.size() - this.expressions.size();
	}


	/**
	 * Parses and matches a single header expression to a request.
	 */
	static class HeaderExpression extends AbstractNameValueExpression<String> {

		public HeaderExpression(String expression) {
			super(expression);
		}

		@Override
		protected boolean isCaseSensitiveName() {
			return false;
		}

		@Override
		protected String parseValue(String valueExpression) {
			return valueExpression;
		}

		@Override
		protected boolean matchName(HttpServletRequest request) {
			return (request.getHeader(this.name) != null);
		}

		@Override
		protected boolean matchValue(HttpServletRequest request) {
			return ObjectUtils.nullSafeEquals(this.value, request.getHeader(this.name));
		}
	}

}
