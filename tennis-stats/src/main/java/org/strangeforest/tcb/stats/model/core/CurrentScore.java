package org.strangeforest.tcb.stats.model.core;

import static java.lang.String.*;

public class CurrentScore {

	private final MatchRules rules;
	private int sets1;
	private int sets2;
	private int games1, games2;
	private int points1, points2;
	private int serve;

	public CurrentScore(MatchRules rules, int sets1, int sets2, int games1, int games2, int points1, int points2, int serve) {
		if (sets1 < 0 || sets2 < 0 || games1 < 0 || games2 < 0 || points1 < 0 || points2 < 0)
			throw new IllegalArgumentException("Negative score");
		this.rules = rules;
		this.sets1 = sets1;
		this.sets2 = sets2;
		this.games1 = games1;
		this.games2 = games2;
		this.points1 = points1;
		this.points2 = points2;
		this.serve = serve;
	}

	public int getSets1() {
		return sets1;
	}

	public int getSets2() {
		return sets2;
	}

	public int getGames1() {
		return games1;
	}

	public int getGames2() {
		return games2;
	}

	public int getPoints1() {
		return points1;
	}

	public int getPoints2() {
		return points2;
	}

	public int getServe() {
		return serve;
	}

	public void incSets1() {
		sets1++;
		var sets = rules.getSets();
		if (sets1 > sets)
			sets1 = sets;
		clearGames();
	}

	public void incSets2() {
		sets2++;
		var sets = rules.getSets();
		if (sets2 > sets)
			sets2 = sets;
		clearGames();
	}

	public void incGames1() {
		games1++;
		var setRules = getSetRules();
		if (games1 >= setRules.getGames() && games1 - games2 >= setRules.getGamesDiff())
			incSets1();
		else
			clearPoints();
		switchServe();
	}

	public void incGames2() {
		games2++;
		var setRules = getSetRules();
		if (games2 >= setRules.getGames() && games2 - games1 >= setRules.getGamesDiff())
			incSets2();
		else
			clearPoints();
		switchServe();
	}

	public void incPoints1() {
		points1++;
		var setRules = getSetRules();
		if (setRules.isTieBreak(games1, games2)) {
			var tieBreakRules = setRules.getTieBreak();
			if (points1 >= tieBreakRules.getPoints() && points1 - points2 >= tieBreakRules.getPointsDiff()) {
				incSets1();
				switchServe();
			}
			else if ((points1 + points2) % 2 == 1)
				switchServe();
		}
		else {
			var gameRules = setRules.getGame();
			if (points1 >= gameRules.getPoints()) {
				if (points1 - points2 >= gameRules.getPointsDiff())
					incGames1();
				else if (points1 == points2) {
					points1--;
					points2--;
				}
			}
		}
	}

	public void incPoints2() {
		points2++;
		var setRules = getSetRules();
		if (setRules.isTieBreak(games1, games2)) {
			var tieBreakRules = setRules.getTieBreak();
			if (points2 >= tieBreakRules.getPoints() && points2 - points1 >= tieBreakRules.getPointsDiff()) {
				incSets2();
				switchServe();
			}
			else if ((points1 + points2) % 2 == 1)
				switchServe();
		}
		else {
			var gameRules = setRules.getGame();
			if (points2 >= gameRules.getPoints()) {
				if (points2 - points1 >= gameRules.getPointsDiff())
					incGames2();
				else if (points1 == points2) {
					points1--;
					points2--;
				}
			}
		}
	}

	private SetRules getSetRules() {
		return rules.getSet(sets1 + sets2 + 1);
	}

	public void clear() {
		sets1 = 0;
		sets2 = 0;
		clearGames();
	}

	public void clearGames() {
		games1 = 0;
		games2 = 0;
		clearPoints();
	}

	public void clearPoints() {
		points1 = 0;
		points2 = 0;
	}

	public void switchServe() {
		serve = 3 - serve;
	}

	@Override public String toString() {
		return format("%1$d:%2$d %3$d:%4$d %5$d:%6$d", sets1, sets2, games1, games2, points1, points2);
	}
}
