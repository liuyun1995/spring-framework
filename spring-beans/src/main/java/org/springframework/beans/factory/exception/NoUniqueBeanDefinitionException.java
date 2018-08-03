package org.springframework.beans.factory.exception;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings("serial")
public class NoUniqueBeanDefinitionException extends NoSuchBeanDefinitionException {

	private int numberOfBeansFound;

	private Collection<String> beanNamesFound;

	public NoUniqueBeanDefinitionException(Class<?> type, int numberOfBeansFound, String message) {
		super(type, message);
		this.numberOfBeansFound = numberOfBeansFound;
	}

	public NoUniqueBeanDefinitionException(Class<?> type, Collection<String> beanNamesFound) {
		this(type, beanNamesFound.size(), "expected single matching bean but found " + beanNamesFound.size() + ": " +
				StringUtils.collectionToCommaDelimitedString(beanNamesFound));
		this.beanNamesFound = beanNamesFound;
	}

	public NoUniqueBeanDefinitionException(Class<?> type, String... beanNamesFound) {
		this(type, Arrays.asList(beanNamesFound));
	}

	@Override
	public int getNumberOfBeansFound() {
		return this.numberOfBeansFound;
	}

	public Collection<String> getBeanNamesFound() {
		return this.beanNamesFound;
	}

}
