package org.strangeforest.tcb.stats.model.records.details;

import java.time.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.datatype.jsr310.deser.*;

import static java.lang.String.*;

public class StreakRecordDetail extends SimpleRecordDetail<Integer> {

	private final int startSeason;
	private final LocalDate startDate;
	private final TournamentEventDetail startEvent;
	private final int endSeason;
	private final LocalDate endDate;
	private final TournamentEventDetail endEvent;
	private final Integer tournamentId;

	public StreakRecordDetail(
      @JsonProperty("value") int value,
      @JsonProperty("start_season") int startSeason,
      @JsonProperty("start_date") @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate startDate,
      @JsonProperty("start_tournament_event_id") int startTournamentEventId,
      @JsonProperty("start_tournament") String startTournament,
      @JsonProperty("start_level") String startLevel,
      @JsonProperty("end_season") int endSeason,
      @JsonProperty("end_date") @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate endDate,
      @JsonProperty("end_tournament_event_id") int endTournamentEventId,
      @JsonProperty("end_tournament") String endTournament,
      @JsonProperty("end_level") String endLevel,
      @JsonProperty("tournament_id") Integer tournamentId
	) {
		super(value);
		this.startSeason = startSeason;
		this.startDate = startDate;
		startEvent = new TournamentEventDetail(startTournamentEventId, startTournament, startLevel);
		this.endSeason = endSeason;
		this.endDate = endDate;
		endEvent = new TournamentEventDetail(endTournamentEventId, endTournament, endLevel);
		this.tournamentId = tournamentId;
	}

	public int getStartSeason() {
		return startSeason;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public TournamentEventDetail getStartEvent() {
		return startEvent;
	}

	public int getEndSeason() {
		return endSeason;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public TournamentEventDetail getEndEvent() {
		return endEvent;
	}

	public Integer getTournamentId() {
		return tournamentId;
	}

	@Override public String toDetailString() {
		return format("%1$d %2$s - %3$d %4$s", startSeason, startEvent.getName(), endSeason, endEvent.getName());
	}
}
