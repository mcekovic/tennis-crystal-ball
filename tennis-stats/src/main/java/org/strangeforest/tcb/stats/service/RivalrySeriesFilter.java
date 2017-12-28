package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;

import static java.lang.String.*;

public class RivalrySeriesFilter {

	private static final String OPPONENT_CRITERIA = " AND o.best_rank <= :opponent";
	private static final String H2H_CRITERIA = " AND r.won %1$s r.lost";
	private static final String MATCHES_CRITERIA = " AND r.won + r.lost >= :matches";

	private final Integer opponent;
	private final Integer h2h;
	private final Integer matches;

	public RivalrySeriesFilter(Integer opponent, Integer h2h, Integer matches) {
		this.opponent = opponent;
		this.h2h = h2h;
		this.matches = matches;
	}

	public String getCriteria() {
		StringBuilder criteria = new StringBuilder();
		if (opponent != null)
			criteria.append(OPPONENT_CRITERIA);
		if (h2h != null)
			criteria.append(format(H2H_CRITERIA, h2h > 0 ? ">" : (h2h < 0 ? "<" : "=")));
		if (matches != null)
			criteria.append(MATCHES_CRITERIA);
		return criteria.toString();
	}

	public MapSqlParameterSource getParams() {
		MapSqlParameterSource params = new MapSqlParameterSource();
		if (opponent != null)
			params.addValue("opponent", opponent);
		if (h2h != null)
			params.addValue("h2h", h2h);
		if (matches != null)
			params.addValue("matches", matches);
		return params;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RivalrySeriesFilter)) return false;
		if (!super.equals(o)) return false;
		RivalrySeriesFilter filter = (RivalrySeriesFilter)o;
		return Objects.equals(opponent, filter.opponent) && Objects.equals(h2h, filter.h2h) && Objects.equals(matches, filter.matches);
	}

	@Override public int hashCode() {
		return Objects.hash(opponent, h2h, matches);
	}

	@Override public final String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("opponent", opponent)
			.add("h2h", h2h)
			.add("matches", matches)
		.toString();
	}
}
