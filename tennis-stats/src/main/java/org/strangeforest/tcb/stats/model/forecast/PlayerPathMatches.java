package org.strangeforest.tcb.stats.model.forecast;

import java.util.*;

import org.strangeforest.tcb.stats.model.*;

public class PlayerPathMatches extends TournamentEventResults {

	public List<TournamentEventMatch> getProbableRoundMatches(ResultRound round) {
		List<TournamentEventMatch> matches = getRoundMatches(round);
		Comparator<TournamentEventMatch> comparator = Comparator.comparing(m -> ((PlayerForecast)m.getLoser()).getRawProbability(round.name()));
		matches.sort(comparator.reversed());
		return matches;
	}
}
