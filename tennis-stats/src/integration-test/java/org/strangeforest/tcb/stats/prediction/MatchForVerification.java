package org.strangeforest.tcb.stats.prediction;

import java.util.*;

import org.strangeforest.tcb.stats.model.*;

public class MatchForVerification {

	public final int winnerId;
	public final int loserId;
	public final Date date;
	public final TournamentLevel level;
	public final Surface surface;
	public final int tournamentId;
	public final Round round;
	public final short best_of;
	public final Double winnerPrice;
	public final Double loserPrice;

	public MatchForVerification(int winnerId, int loserId, Date date, String level, String surface, int tournamentId, String round, short best_of, Double winnerPrice, Double loserPrice) {
		this.winnerId = winnerId;
		this.loserId = loserId;
		this.date = date;
		this.level = TournamentLevel.decode(level);
		this.surface = Surface.safeDecode(surface);
		this.tournamentId = tournamentId;
		this.round = Round.decode(round);
		this.best_of = best_of;
		this.winnerPrice = winnerPrice;
		this.loserPrice = loserPrice;
	}
}
