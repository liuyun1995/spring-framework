package org.springframework.context.expression;

import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

/**
 * EL property accessor that knows how to traverse the beans and contextual objects
 * of a Spring {@link org.springframework.beans.factory.config.BeanExpressionContext}.
 *
 * @author Juergen Hoeller
 * @author Andy Clement
 * @since 3.0
 */
public class BeanExpressionContextAccessor implements PropertyAccessor {

	@Override
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		return ((BeanExpressionContext) target).containsObject(name);
	}

	@Override
	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		return new TypedValue(((BeanExpressionContext) target).getObject(name));
	}

	@Override
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return false;
	}

	@Override
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
		throw new AccessException("Beans in a BeanFactory are read-only");
	}

	@Override
	public Class<?>[] getSpecificTargetClasses() {
		return new Class<?>[] {BeanExpressionContext.class};
	}

}
