package org.strangeforest.tcb.stats.model.core;

import static org.strangeforest.tcb.stats.model.core.TieBreakRules.*;

public class SetRules {

	public static final SetRules COMMON_SET = new SetRules(6, COMMON_TIE_BREAK);
	public static final SetRules NO_TB_SET = new SetRules(null, null);
	public static final SetRules AO_5TH_SET = new SetRules(6, SUPER_TIE_BREAK);
	public static final SetRules WB_5TH_SET = new SetRules(12, COMMON_TIE_BREAK);

	private final int games;
	private final int gamesDiff;
	private final Integer tieBreakAt;
	private final TieBreakRules tieBreak;

	public SetRules(Integer tieBreakAt, TieBreakRules tieBreak) {
		this(6, 2, tieBreakAt, tieBreak);
	}

	public SetRules(int games, int gamesDiff, Integer tieBreakAt, TieBreakRules tieBreak) {
		this.games = games;
		this.gamesDiff = gamesDiff;
		this.tieBreakAt = tieBreakAt;
		this.tieBreak = tieBreak;
	}

	public int getGames() {
		return games;
	}

	public int getGamesDiff() {
		return gamesDiff;
	}

	public Integer getTieBreakAt() {
		return tieBreakAt;
	}

	public TieBreakRules getTieBreak() {
		return tieBreak;
	}

	public boolean isTieBreak(int games1, int games2) {
		return tieBreakAt != null && games1 == games2 && games1 == tieBreakAt;
	}
}
