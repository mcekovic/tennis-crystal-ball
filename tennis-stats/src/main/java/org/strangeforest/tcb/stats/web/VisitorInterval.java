package org.strangeforest.tcb.stats.web;

public enum VisitorInterval {

	HOUR("1 HOUR", "Hour"),
	DAY("1 DAY", "Day"),
	WEEK("1 WEEK", "Week"),
	MONTH("1 MONTH", "Month");

	private final String expression;
	private final String caption;

	VisitorInterval(String expression, String caption) {
		this.expression = expression;
		this.caption = caption;
	}

	public String getExpression() {
		return expression;
	}

	public String getCaption() {
		return caption;
	}
}
