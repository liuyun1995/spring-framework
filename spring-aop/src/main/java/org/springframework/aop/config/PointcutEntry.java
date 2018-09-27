package org.springframework.aop.config;

import org.springframework.beans.factory.parsing.ParseState;

//切入点实体
public class PointcutEntry implements ParseState.Entry {

	private final String name;

	public PointcutEntry(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Pointcut '" + this.name + "'";
	}

}
