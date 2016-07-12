package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class StreakRecordDetail extends IntegerRecordDetail {

	private final int startSeason;
	private final TournamentEventDetail startEvent;
	private final int endSeason;
	private final TournamentEventDetail endEvent;

	public StreakRecordDetail(
      @JsonProperty("value") int value,
      @JsonProperty("start_season") int startSeason,
      @JsonProperty("start_tournament_event_id") int startTournamentEventId,
      @JsonProperty("start_tournament") String startTournament,
      @JsonProperty("start_level") String startLevel,
      @JsonProperty("end_season") int endSeason,
      @JsonProperty("end_tournament_event_id") int endTournamentEventId,
      @JsonProperty("end_tournament") String endTournament,
      @JsonProperty("end_level") String endLevel
	) {
		super(value);
		this.startSeason = startSeason;
		startEvent = new TournamentEventDetail(startTournamentEventId, startTournament, startLevel);
		this.endSeason = endSeason;
		endEvent = new TournamentEventDetail(endTournamentEventId, endTournament, endLevel);
	}

	public int getStartSeason() {
		return startSeason;
	}

	public TournamentEventDetail getStartEvent() {
		return startEvent;
	}

	public int getEndSeason() {
		return endSeason;
	}

	public TournamentEventDetail getEndEvent() {
		return endEvent;
	}

	@Override public String toDetailString() {
		return format("%1$d %2$s - %3$d %4$s", startSeason, startEvent.getName(), endSeason, endEvent.getName());
	}
}
