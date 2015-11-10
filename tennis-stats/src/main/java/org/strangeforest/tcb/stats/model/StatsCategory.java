package org.strangeforest.tcb.stats.model;

public final class StatsCategory {

	public enum Type {COUNT, PERCENTAGE, RATIO}

	private final String name;
	private final String expression;
	private final Type type;
	private final boolean needsStats;

	public StatsCategory(String name, String expression, Type type, boolean needsStats) {
		this.name = name;
		this.expression = expression;
		this.type = type;
		this.needsStats = needsStats;
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

	public boolean isNeedsStats() {
		return needsStats;
	}
}
