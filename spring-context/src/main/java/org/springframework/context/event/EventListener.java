package org.springframework.context.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.annotation.AliasFor;

//事件监听者注解
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventListener {

	/**
	 * Alias for {@link #classes}.
	 */
	@AliasFor("classes")
	Class<?>[] value() default {};

	/**
	 * The event classes that this listener handles.
	 * <p>If this attribute is specified with a single value, the
	 * annotated method may optionally accept a single parameter.
	 * However, if this attribute is specified with multiple values,
	 * the annotated method must <em>not</em> declare any parameters.
	 */
	@AliasFor("value")
	Class<?>[] classes() default {};

	/**
	 * Spring Expression Language (SpEL) attribute used for making the
	 * event handling conditional.
	 * <p>Default is {@code ""}, meaning the event is always handled.
	 * <p>The SpEL expression evaluates against a dedicated context that
	 * provides the following meta-data:
	 * <ul>
	 * <li>{@code #root.event}, {@code #root.args} for
	 * references to the {@link ApplicationEvent} and method arguments
	 * respectively.</li>
	 * <li>Method arguments can be accessed by index. For instance the
	 * first argument can be accessed via {@code #root.args[0]}, {@code #p0}
	 * or {@code #a0}. Arguments can also be accessed by name if that
	 * information is available.</li>
	 * </ul>
	 */
	String condition() default "";

}
