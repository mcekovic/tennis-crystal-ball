package org.strangeforest.tcb.stats.visitors;

public enum VisitorInterval {

	HOUR("1 hour", "Last Hour"),
	DAY("1 day", "Last Day"),
	WEEK("1 week", "Last Week"),
	MONTH("1 month", "Last Month"),
	YEAR("1 year", "Last Year");

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
