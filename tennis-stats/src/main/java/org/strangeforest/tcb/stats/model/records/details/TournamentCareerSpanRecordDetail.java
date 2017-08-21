package org.strangeforest.tcb.stats.model.records.details;

import java.time.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.datatype.jsr310.deser.*;

import static java.lang.String.*;

public class TournamentCareerSpanRecordDetail extends CareerSpanRecordDetail {

	private final TournamentEventDetail startEvent;
	private final TournamentEventDetail endEvent;

	public TournamentCareerSpanRecordDetail(
		@JsonProperty("value") String value,
		@JsonProperty("start_date") @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate startDate,
		@JsonProperty("start_tournament_event_id") int startTournamentEventId,
		@JsonProperty("start_tournament") String startTournament,
		@JsonProperty("start_level") String startLevel,
		@JsonProperty("end_date") @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate endDate,
		@JsonProperty("end_tournament_event_id") int endTournamentEventId,
		@JsonProperty("end_tournament") String endTournament,
		@JsonProperty("end_level") String endLevel
	) {
		super(value, startDate, endDate);
		startEvent = new TournamentEventDetail(startTournamentEventId, startTournament, startLevel);
		endEvent = new TournamentEventDetail(endTournamentEventId, endTournament, endLevel);
	}

	public TournamentEventDetail getStartEvent() {
		return startEvent;
	}

	public TournamentEventDetail getEndEvent() {
		return endEvent;
	}

	@Override public String toDetailString() {
		return format("%1$td-%1$tm-%1$tY %2$s - %3$td-%3$tm-%3$tY %4$s", getStartDate(), startEvent.getName(), getEndDate(), endEvent.getName());
	}
}
