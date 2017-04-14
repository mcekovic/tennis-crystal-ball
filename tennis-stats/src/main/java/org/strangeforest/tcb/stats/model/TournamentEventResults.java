package org.strangeforest.tcb.stats.model;

import java.util.*;

public class TournamentEventResults {

	private enum Round {

		RR("Round-Robin"),
		R128("Round of 128"),
		R64("Round of 64"),
		R32("Round of 32"),
		R16("Round of 16"),
		QF("Quarter-Final"),
		SF("Semi-Final"),
		BR("For Bronze Medal"),
		F("Final");

		private String description;

		Round(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}

	private final Map<Short, TournamentEventMatch> matches;
	private final Map<Round, List<TournamentEventMatch>> matchesByRound;

	public TournamentEventResults() {
		this.matches = new TreeMap<>();
		matchesByRound = new TreeMap<>();
	}

	public void addMatch(short matchNum, TournamentEventMatch match) {
		matches.put(matchNum, match);
		Round round = Round.valueOf(match.getRound());
		matchesByRound.computeIfAbsent(round, r -> new ArrayList<>()).add(match);
	}

	public Collection<Round> getRounds() {
		return matchesByRound.keySet();
	}

	public List<TournamentEventMatch> getRoundMatches(Round round) {
		return matchesByRound.get(round);
	}

	public int getMaxRoundMatches() {
		return matchesByRound.values().stream().mapToInt(List::size).max().getAsInt();
	}

	public boolean isEmpty() {
		return matches.isEmpty();
	}
}
