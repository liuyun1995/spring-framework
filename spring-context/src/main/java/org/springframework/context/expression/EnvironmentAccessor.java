package org.springframework.context.expression;

import org.springframework.core.env.Environment;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

/**
 * Read-only EL property accessor that knows how to retrieve keys
 * of a Spring {@link Environment} instance.
 *
 * @author Chris Beams
 * @since 3.1
 */
public class EnvironmentAccessor implements PropertyAccessor {

	@Override
	public Class<?>[] getSpecificTargetClasses() {
		return new Class<?>[] {Environment.class};
	}

	/**
	 * Can read any {@link Environment}, thus always returns true.
	 * @return true
	 */
	@Override
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		return true;
	}

	/**
	 * Access the given target object by resolving the given property name against the given target
	 * environment.
	 */
	@Override
	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		return new TypedValue(((Environment) target).getProperty(name));
	}

	/**
	 * Read-only: returns {@code false}.
	 */
	@Override
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return false;
	}

	/**
	 * Read-only: no-op.
	 */
	@Override
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
	}

}
