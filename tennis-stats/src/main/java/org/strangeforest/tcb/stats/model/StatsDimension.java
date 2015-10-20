package org.strangeforest.tcb.stats.model;

public final class StatsDimension {

	public enum Type {COUNT, PERCENTAGE, RATIO}

	private final String name;
	private final String expression;
	private final Type type;

	public StatsDimension(String name, String expression, Type type) {
		this.name = name;
		this.expression = expression;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getExpression() {
		return expression;
	}

	public Type getType() {
		return type;
	}
}
