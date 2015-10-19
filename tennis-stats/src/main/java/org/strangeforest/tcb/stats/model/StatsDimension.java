package org.strangeforest.tcb.stats.model;

public final class StatsDimension {

	private final String name;
	private final String expression;
	private final boolean pct;

	public StatsDimension(String name, String expression, boolean pct) {
		this.name = name;
		this.expression = expression;
		this.pct = pct;
	}

	public String getName() {
		return name;
	}

	public String getExpression() {
		return expression;
	}

	public boolean isPct() {
		return pct;
	}
}
