package org.strangeforest.tcb.stats.service;

import java.util.*;

public class PlayerListSeasonFilter extends PlayerListFilter {

	private final Integer season;

	private static final String SEASON_CRITERION = " AND season = ?";

	public PlayerListSeasonFilter(String searchPhrase, Integer season) {
		super(searchPhrase);
		this.season = season;
	}

	public boolean hasSeason() {
		return season != null;
	}

	@Override protected void appendCriteria(StringBuilder criteria) {
		if (season != null)
			criteria.append(SEASON_CRITERION);
		super.appendCriteria(criteria);
	}

	@Override protected void addParams(List<Object> params) {
		if (season != null)
			params.add(season);
		super.addParams(params);
	}
}
