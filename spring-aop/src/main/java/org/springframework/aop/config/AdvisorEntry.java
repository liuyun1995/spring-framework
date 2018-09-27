package org.springframework.aop.config;

import org.springframework.beans.factory.parsing.ParseState;

/**
 * {@link ParseState} entry representing an advisor.
 *
 * @author Mark Fisher
 * @since 2.0
 */
public class AdvisorEntry implements ParseState.Entry {

	private final String name;

	public AdvisorEntry(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Advisor '" + this.name + "'";
	}

}
