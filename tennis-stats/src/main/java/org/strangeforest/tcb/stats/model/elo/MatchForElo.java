package org.strangeforest.tcb.stats.model.elo;

import java.time.*;

public class MatchForElo {

	public final long matchId;
	public final int winnerId;
	public final int loserId;
	public final LocalDate endDate;
	public final String level;
	public final short bestOf;
	public final String surface;
	public final boolean indoor;
	public final String round;
	public final String outcome;
	public final int wSets, lSets;
	public final int wGames, lGames;
	public final int wSvGms, lSvGms;
	public final int wRtGms, lRtGms;
	public final int wTbs, lTbs;
	public final boolean hasStats;

	public MatchForElo(long matchId, int winnerId, int loserId, LocalDate endDate, String level, short bestOf, String surface, boolean indoor, String round, String outcome, int wSets, int lSets, int wGames, int lGames, int wSvGms, int lSvGms, int wRtGms, int lRtGms, int wTbs, int lTbs, boolean hasStats) {
		this.matchId = matchId;
		this.winnerId = winnerId;
		this.loserId = loserId;
		this.endDate = endDate;
		this.level = level;
		this.bestOf = bestOf;
		this.surface = surface;
		this.indoor = indoor;
		this.round = round;
		this.outcome = outcome;
		this.wSets = wSets;
		this.lSets = lSets;
		this.wGames = wGames;
		this.lGames = lGames;
		this.wSvGms = wSvGms;
		this.lSvGms = lSvGms;
		this.wRtGms = wRtGms;
		this.lRtGms = lRtGms;
		this.wTbs = wTbs;
		this.lTbs = lTbs;
		this.hasStats = hasStats;
	}
}
