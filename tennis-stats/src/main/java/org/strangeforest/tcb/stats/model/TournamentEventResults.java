package org.strangeforest.tcb.stats.model;

import java.util.ArrayList;
import java.util.*;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

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

	public Collection<TournamentEventMatch> getMatches() {
		return matches.values();
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
		return matchesByRound.values().stream().mapToInt(List::size).max().orElse(0);
	}

	public List<MatchPlayerEx> getPlayers() {
		if (isEmpty())
			return emptyList();
		Round firstRound = getRounds().iterator().next();
		return getRoundMatches(firstRound).stream()
			.map(match -> asList(match.getWinner(), match.getLoser()))
			.flatMap(Collection::stream)
			.filter(player -> player.getId() > 0)
			.sorted(comparing(MatchPlayerEx::getSeed, nullsLast(naturalOrder())).thenComparing(MatchPlayerEx::getName, nullsLast(naturalOrder())))
			.collect(toList());
	}

	public boolean isEmpty() {
		return matches.isEmpty();
	}
}
