package org.springframework.beans.factory.support;

import org.springframework.beans.BeanMetadataAttributeAccessor;
import org.springframework.util.Assert;

@SuppressWarnings("serial")
public class AutowireCandidateQualifier extends BeanMetadataAttributeAccessor {

	public static String VALUE_KEY = "value";

	private final String typeName;
	
	public AutowireCandidateQualifier(Class<?> type) {
		this(type.getName());
	}
	
	public AutowireCandidateQualifier(String typeName) {
		Assert.notNull(typeName, "Type name must not be null");
		this.typeName = typeName;
	}

	/**
	 * Construct a qualifier to match against an annotation of the
	 * given type whose {@code value} attribute also matches
	 * the specified value.
	 * @param type the annotation type
	 * @param value the annotation value to match
	 */
	public AutowireCandidateQualifier(Class<?> type, Object value) {
		this(type.getName(), value);
	}

	/**
	 * Construct a qualifier to match against an annotation of the
	 * given type name whose {@code value} attribute also matches
	 * the specified value.
	 * <p>The type name may match the fully-qualified class name of
	 * the annotation or the short class name (without the package).
	 * @param typeName the name of the annotation type
	 * @param value the annotation value to match
	 */
	public AutowireCandidateQualifier(String typeName, Object value) {
		Assert.notNull(typeName, "Type name must not be null");
		this.typeName = typeName;
		setAttribute(VALUE_KEY, value);
	}


	/**
	 * Retrieve the type name. This value will be the same as the
	 * type name provided to the constructor or the fully-qualified
	 * class name if a Class instance was provided to the constructor.
	 */
	public String getTypeName() {
		return this.typeName;
	}

}
