package org.strangeforest.tcb.stats.service;

import java.util.*;
import java.util.Objects;
import java.util.regex.*;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;

public class RivalrySeriesFilter {

	public static final RivalrySeriesFilter ALL = new RivalrySeriesFilter(null, null, null, null);

	private static final String OPPONENT_BEST_RANK_CRITERIA = " AND o.best_rank <= :opponentBestRank";
	private static final String OPPONENT_COUNTRY_CRITERIA = " AND o.country_id IN (:opponentCountryIds)";
	private static final String H2H_CRITERIA = " AND r.won %1$s r.lost";
	private static final String MATCHES_CRITERIA = " AND r.won + r.lost >= :matches";

	private static final Pattern INTEGER = Pattern.compile("\\d+");

	private final Integer opponentBestRank;
	private final Opponent opponent;
	private final Collection<String> opponentCountryIds;
	private final Integer h2h;
	private final Integer matches;

	public RivalrySeriesFilter(String opponent, Collection<String> opponentCountryIds, Integer h2h, Integer matches) {
		boolean hasOpponent = !isNullOrEmpty(opponent);
		boolean isBestRank = hasOpponent && INTEGER.matcher(opponent).matches();
		this.opponentBestRank = isBestRank ? Integer.parseInt(opponent) : null;
		this.opponent = hasOpponent && !isBestRank ? Opponent.valueOf(opponent) : null;
		this.opponentCountryIds = opponentCountryIds != null ? opponentCountryIds : Collections.emptyList();
		this.h2h = h2h;
		this.matches = matches;
	}

	public String getCriteria() {
		StringBuilder criteria = new StringBuilder();
		if (opponentBestRank != null)
			criteria.append(OPPONENT_BEST_RANK_CRITERIA);
		if (opponent != null)
			criteria.append(opponent.getStatsCriterion());
		if (!opponentCountryIds.isEmpty())
			criteria.append(OPPONENT_COUNTRY_CRITERIA);
		if (h2h != null)
			criteria.append(format(H2H_CRITERIA, h2h > 0 ? ">" : (h2h < 0 ? "<" : "=")));
		if (matches != null)
			criteria.append(MATCHES_CRITERIA);
		return criteria.toString();
	}

	public MapSqlParameterSource getParams() {
		MapSqlParameterSource params = new MapSqlParameterSource();
		if (opponentBestRank != null)
			params.addValue("opponentBestRank", opponentBestRank);
		if (!opponentCountryIds.isEmpty())
			params.addValue("opponentCountryIds", opponentCountryIds);
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
		return Objects.equals(opponentBestRank, filter.opponentBestRank) && opponent == filter.opponent && opponentCountryIds.equals(filter.opponentCountryIds) && Objects.equals(h2h, filter.h2h) && Objects.equals(matches, filter.matches);
	}

	@Override public int hashCode() {
		return Objects.hash(opponentBestRank, opponent, opponentCountryIds, h2h, matches);
	}

	@Override public final String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("opponentBestRank", opponentBestRank)
			.add("opponent", opponent)
			.add("countryIds", opponentCountryIds.isEmpty() ? null : opponentCountryIds)
			.add("h2h", h2h)
			.add("matches", matches)
		.toString();
	}
}
