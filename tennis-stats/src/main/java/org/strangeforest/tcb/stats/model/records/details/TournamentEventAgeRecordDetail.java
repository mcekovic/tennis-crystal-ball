package org.strangeforest.tcb.stats.model.records.details;

import org.strangeforest.tcb.stats.model.records.*;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class TournamentEventAgeRecordDetail implements RecordDetail {

	private final String age;
	private final int season;
	private final int tournamentEventId;
	private final String tournament;
	private final String level;

	public TournamentEventAgeRecordDetail(
		@JsonProperty("age") String age,
		@JsonProperty("season") int season,
		@JsonProperty("tournament_event_id") int tournamentEventId,
		@JsonProperty("tournament") String tournament,
		@JsonProperty("level") String level
	) {
		this.age = age;
		this.season = season;
		this.tournamentEventId = tournamentEventId;
		this.tournament = tournament;
		this.level = level;
	}

	public String getAge() {
		return age;
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

	@Override public String toString() {
		return format("%1$s (%2$d %3$s)", age, season, tournament);
	}
}
