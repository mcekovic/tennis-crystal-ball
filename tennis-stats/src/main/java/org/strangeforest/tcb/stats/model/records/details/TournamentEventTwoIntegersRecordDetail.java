package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class TournamentEventTwoIntegersRecordDetail extends TournamentEventIntegerRecordDetail {

	private final int value2;

	public TournamentEventTwoIntegersRecordDetail(
      @JsonProperty("value") int value,
      @JsonProperty("value2") int value2,
      @JsonProperty("season") int season,
      @JsonProperty("tournament_event_id") int tournamentEventId,
      @JsonProperty("tournament") String tournament,
      @JsonProperty("level") String level,
      @JsonProperty("matches") int matches
	) {
		super(value, season, tournamentEventId, tournament, level, matches);
		this.value2 = value2;
	}

	public int getValue2() {
		return value2;
	}
}
