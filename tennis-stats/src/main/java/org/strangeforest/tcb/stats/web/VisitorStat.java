package org.strangeforest.tcb.stats.web;

public enum VisitorStat {

	VISITS("count(visitor_id)", "Visits"),
	ACTIVE_VISITORS("sum(CASE WHEN active THEN 1 ELSE 0 END)", "Active visitors"),
	VISITS_PER_IP("round(count(visitor_id)::REAL / count(DISTINCT ip_address), 2)", "Visits per IP"),
	HITS("sum(hits)", "Hits"),
	HITS_PER_VISIT("avg(hits)::INTEGER", "Hits per visit");

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
