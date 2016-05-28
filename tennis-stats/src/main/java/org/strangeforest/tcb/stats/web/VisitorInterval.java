package org.strangeforest.tcb.stats.web;

public enum VisitorInterval {

	HOUR("1 HOUR", "Last Hour"),
	DAY("1 DAY", "Last Day"),
	WEEK("1 WEEK", "Last Week"),
	MONTH("1 MONTH", "Last Month"),
	YEAR("1 YEAR", "Last Year");

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
