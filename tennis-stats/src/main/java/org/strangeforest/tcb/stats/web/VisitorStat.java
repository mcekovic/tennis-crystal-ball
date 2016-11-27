package org.strangeforest.tcb.stats.web;

public enum VisitorStat {

	HITS("sum(hits)", "Hits"),
	VISITS("count(visitor_id)", "Visits"),
	ACTIVE_VISITORS("sum(CASE WHEN active THEN 1 ELSE 0 END)", "Active visitors"),
	HITS_PER_VISIT("avg(hits)::INTEGER", "Hits per visit"),
	VISITS_PER_IP("round(count(visitor_id)::NUMERIC / count(DISTINCT ip_address), 2)", "Visits per IP");

	private final String expression;
	private final String caption;

	VisitorStat(String expression, String caption) {
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
