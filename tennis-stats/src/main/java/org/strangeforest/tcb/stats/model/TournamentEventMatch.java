package org.strangeforest.tcb.stats.model;

public class TournamentEventMatch {

	private final long id;
	private final String round;
	private final MatchPlayerEx winner;
	private final MatchPlayerEx loser;
	private final String score;

	public TournamentEventMatch(long id, String round, MatchPlayerEx winner, MatchPlayerEx loser, String score) {
		this.id = id;
		this.round = round;
		this.winner = winner;
		this.loser = loser;
		this.score = score;
	}

	public long getId() {
		return id;
	}

	public String getRound() {
		return round;
	}

	public MatchPlayerEx getWinner() {
		return winner;
	}

	public MatchPlayerEx getLoser() {
		return loser;
	}

	public String getScore() {
		return score;
	}
}
