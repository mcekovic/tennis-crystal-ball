package org.strangeforest.tcb.stats.model;

import java.time.*;

import org.strangeforest.tcb.stats.model.core.*;

import static java.lang.String.*;
import static org.strangeforest.tcb.stats.controller.StatsFormatUtil.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;
import static org.strangeforest.tcb.util.EnumUtil.*;

public class TopMatchStatsRow extends PlayerRow {

	private final PlayerRow opponent;
	private final LocalDate date;
	private final int tournamentEventId;
	private final String tournament;
	private final String level;
	private final int bestOf;
	private final String surface;
	private final boolean indoor;
	private final Integer speed;
	private final String round;
	private final String score;
	private final String outcome;
	private final boolean winner;
	private final double value;
	private final StatsCategory.Type categoryType;

	public TopMatchStatsRow(int rank, int playerId, String name, String countryId, Boolean active, PlayerRow opponent, LocalDate date, int tournamentEventId, String tournament, String level, int bestOf, String surface, boolean indoor, Integer speed, String round, String score, String outcome, boolean winner, double value, StatsCategory.Type categoryType) {
		super(rank, playerId, name, countryId, active);
		this.opponent = opponent;
		this.date = date;
		this.tournamentEventId = tournamentEventId;
		this.tournament = tournament;
		this.level = level;
		this.bestOf = bestOf;
		this.surface = surface;
		this.indoor = indoor;
		this.speed = speed;
		this.round = round;
		this.score = score;
		this.outcome = outcome;
		this.winner = winner;
		this.value = value;
		this.categoryType = categoryType;
	}

	public PlayerRow getOpponent() {
		return opponent;
	}

	public LocalDate getDate() {
		return date;
	}

	public int getTournamentEventId() {
		return tournamentEventId;
	}

	public String getTournament() {
		return tournament;
	}

	public String getLevel() {
		return level;
	}

	public int getBestOf() {
		return bestOf;
	}

	public String getSurface() {
		return surface;
	}

	public boolean isIndoor() {
		return indoor;
	}

	public Integer getSpeed() {
		return speed;
	}

	public String getRound() {
		return round;
	}

	public String getScore() {
		return score;
	}

	public String getOutcome() {
		return outcome;
	}

	public boolean isWinner() {
		return winner;
	}

	public String getValue() {
		switch (categoryType) {
			case COUNT: return valueOf((int)value);
			case PERCENTAGE: return format("%5.1f%%", PCT * value);
			case RATIO1: return format("%7.0f", value);
			case RATIO2: return format("%7.1f", value);
			case RATIO3: return format("%7.2f", value);
			case TIME: return formatTime((int)value);
			default: throw unknownEnum(categoryType);
		}
	}
}
