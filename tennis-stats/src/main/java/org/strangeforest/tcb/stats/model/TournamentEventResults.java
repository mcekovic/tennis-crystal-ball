package org.strangeforest.tcb.stats.model;

import java.util.*;

public class TournamentEventResults {

	private enum ResultRound {

		RR(Round.RR),
		R128(Round.R128),
		R64(Round.R64),
		R32(Round.R32),
		R16(Round.R16),
		QF(Round.QF),
		SF(Round.SF),
		BR(Round.BR),
		F(Round.F);

		private Round round;

		ResultRound(Round round) {
			this.round = round;
		}

		public String getText() {
			return round.getText();
		}
	}

	private final Map<Short, TournamentEventMatch> matches;
	private final Map<ResultRound, List<TournamentEventMatch>> matchesByRound;

	public TournamentEventResults() {
		this.matches = new TreeMap<>();
		matchesByRound = new TreeMap<>();
	}

	public Collection<TournamentEventMatch> getMatches() {
		return matches.values();
	}

	public void addMatch(short matchNum, TournamentEventMatch match) {
		matches.put(matchNum, match);
		ResultRound round = ResultRound.valueOf(match.getRound());
		matchesByRound.computeIfAbsent(round, r -> new ArrayList<>()).add(match);
	}

	public Collection<ResultRound> getRounds() {
		return matchesByRound.keySet();
	}

	public List<TournamentEventMatch> getRoundMatches(ResultRound round) {
		return matchesByRound.get(round);
	}

	public int getMaxRoundMatches() {
		return matchesByRound.values().stream().mapToInt(List::size).max().orElse(0);
	}

	public boolean isEmpty() {
		return matches.isEmpty();
	}
}
