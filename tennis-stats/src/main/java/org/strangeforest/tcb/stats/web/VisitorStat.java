package org.strangeforest.tcb.stats.web;

public enum VisitorStat {

	HITS("coalesce(sum(hits), 0)", "Hits"),
	VISITS("count(visitor_id)", "Visits"),
	ACTIVE_VISITORS("count(visitor_id) FILTER (WHERE active)", "Active visitors"),
	HITS_PER_VISIT("coalesce(avg(hits), 0)::INTEGER", "Hits per visit"),
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
