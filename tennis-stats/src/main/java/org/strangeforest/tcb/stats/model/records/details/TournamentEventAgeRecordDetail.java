package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class TournamentEventAgeRecordDetail extends SimpleRecordDetail<String> {

	private final int season;
	private final int tournamentEventId;
	private final String tournament;
	private final String level;

	public TournamentEventAgeRecordDetail(
		@JsonProperty("value") String value,
		@JsonProperty("season") int season,
		@JsonProperty("tournament_event_id") int tournamentEventId,
		@JsonProperty("tournament") String tournament,
		@JsonProperty("level") String level
	) {
		super(value);
		this.season = season;
		this.tournamentEventId = tournamentEventId;
		this.tournament = tournament;
		this.level = level;
	}

	public int getSeason() {
		return season;
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

	@Override public String toDetailString() {
		return format("%1$d %2$s", season, tournament);
	}
}
