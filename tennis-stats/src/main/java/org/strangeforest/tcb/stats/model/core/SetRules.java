package org.strangeforest.tcb.stats.model.core;

import java.util.*;

import static org.strangeforest.tcb.stats.model.core.GameRules.*;
import static org.strangeforest.tcb.stats.model.core.TieBreakRules.*;

public class SetRules {

	public static final SetRules COMMON_SET = new SetRules(6, COMMON_TIE_BREAK);
	public static final SetRules NO_TB_SET = new SetRules(null, null);
	public static final SetRules AO_5TH_SET = new SetRules(6, SUPER_TIE_BREAK);
	public static final SetRules WB_5TH_SET = new SetRules(12, COMMON_TIE_BREAK);

	private final int games;
	private final int gamesDiff;
	private final GameRules game;
	private final Integer tieBreakAt;
	private final TieBreakRules tieBreak;

	public SetRules(Integer tieBreakAt, TieBreakRules tieBreak) {
		this(6, 2, COMMON_GAME, tieBreakAt, tieBreak);
	}

	public SetRules(int games, int gamesDiff, GameRules game, Integer tieBreakAt, TieBreakRules tieBreak) {
		this.games = games;
		this.gamesDiff = gamesDiff;
		this.game = game;
		this.tieBreakAt = tieBreakAt;
		this.tieBreak = tieBreak;
	}

	public int getGames() {
		return games;
	}

	public int getGamesDiff() {
		return gamesDiff;
	}

	public GameRules getGame() {
		return game;
	}

	public boolean hasTieBreak() {
		return tieBreakAt != null;
	}

	public Integer getTieBreakAt() {
		return tieBreakAt;
	}

	public TieBreakRules getTieBreak() {
		return tieBreak;
	}

	public boolean isTieBreak(int games1, int games2) {
		return hasTieBreak() && games1 == games2 && games1 >= tieBreakAt;
	}


	// Object Methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SetRules)) return false;
		var rules = (SetRules) o;
		return games == rules.games && gamesDiff == rules.gamesDiff &&	Objects.equals(game, rules.game) &&
			Objects.equals(tieBreakAt, rules.tieBreakAt) && Objects.equals(tieBreak, rules.tieBreak);
	}

	@Override public int hashCode() {
		return Objects.hash(games, gamesDiff, game, tieBreakAt, tieBreak);
	}
}
