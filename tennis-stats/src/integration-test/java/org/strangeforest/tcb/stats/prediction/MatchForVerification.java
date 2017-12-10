package org.strangeforest.tcb.stats.prediction;

import java.time.*;

import org.strangeforest.tcb.stats.model.core.*;

public class MatchForVerification {

	public final int winnerId;
	public final int loserId;
	public final LocalDate date;
	public final int tournamentId;
	public final int tournamentEventId;
	public final TournamentLevel level;
	public final short bestOf;
	public final Surface surface;
	public final boolean indoor;
	public final Round round;
	public final Double winnerPrice;
	public final Double loserPrice;

	public MatchForVerification(int winnerId, int loserId, LocalDate date, int tournamentId, int tournamentEventId, String level, short bestOf, String surface, boolean indoor, String round, Double winnerPrice, Double loserPrice) {
		this.winnerId = winnerId;
		this.loserId = loserId;
		this.date = date;
		this.tournamentId = tournamentId;
		this.tournamentEventId = tournamentEventId;
		this.level = TournamentLevel.decode(level);
		this.bestOf = bestOf;
		this.surface = Surface.safeDecode(surface);
		this.indoor = indoor;
		this.round = Round.decode(round);
		this.winnerPrice = winnerPrice;
		this.loserPrice = loserPrice;
	}
}
