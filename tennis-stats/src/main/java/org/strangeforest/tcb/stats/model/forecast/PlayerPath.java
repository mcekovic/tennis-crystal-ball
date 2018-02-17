package org.strangeforest.tcb.stats.model.forecast;

import java.util.*;

import org.strangeforest.tcb.stats.model.*;

public class PlayerPath {

	private final PlayerForecast player;
	private final TournamentEventResults completedMatches;
	private final PlayerPathMatches probableMatches;
	private final List<MatchPlayer> players;

	public PlayerPath(PlayerForecast player, TournamentEventResults completedMatches, PlayerPathMatches probableMatches, List<MatchPlayer> players) {
		this.player = player;
		this.completedMatches = completedMatches;
		this.probableMatches = probableMatches;
		this.players = players;
	}

	public PlayerForecast getPlayer() {
		return player;
	}

	public TournamentEventResults getCompletedMatches() {
		return completedMatches;
	}

	public PlayerPathMatches getProbableMatches() {
		return probableMatches;
	}

	public List<MatchPlayer> getPlayers() {
		return players;
	}

	public boolean isEmpty() {
		return completedMatches.isEmpty() && probableMatches.isEmpty();
	}
}
