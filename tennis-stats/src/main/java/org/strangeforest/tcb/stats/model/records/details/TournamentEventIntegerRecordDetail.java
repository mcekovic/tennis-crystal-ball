package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class TournamentEventIntegerRecordDetail extends IntegerRecordDetail {

	private final int season;
	private final int tournamentEventId;
	private final String tournament;
	private final String level;
	private final int matches;

	public TournamentEventIntegerRecordDetail(
      @JsonProperty("value") int value,
      @JsonProperty("season") int season,
      @JsonProperty("tournament_event_id") int tournamentEventId,
      @JsonProperty("tournament") String tournament,
      @JsonProperty("level") String level,
      @JsonProperty("matches") int matches
	) {
		super(value);
		this.season = season;
		this.tournamentEventId = tournamentEventId;
		this.tournament = tournament;
		this.level = level;
		this.matches = matches;
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

	public int getMatches() {
		return matches;
	}

	@Override public String toDetailString() {
		return format("%1$d %2$s", season, tournament);
	}
}
