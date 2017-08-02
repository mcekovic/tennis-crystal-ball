package org.strangeforest.tcb.stats.model;

import java.util.*;

public class PlayerDiffRankingsRow extends PlayerRankingsRow {

	private final Integer rankDiff;
	private final Integer pointsDiff;

	public PlayerDiffRankingsRow(int rank, int playerId, String name, String countryId, int points, int bestRank, Date bestRankDate, Integer rankDiff, Integer pointsDiff) {
		super(rank, playerId, name, countryId, null, points, bestRank, bestRankDate);
		this.rankDiff = rankDiff;
		this.pointsDiff = pointsDiff;
	}

	public Integer getRankDiff() {
		return rankDiff;
	}

	public Integer getPointsDiff() {
		return pointsDiff;
	}
}
