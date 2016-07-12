package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class TournamentCareerSpanRecordDetail extends CareerSpanRecordDetail {

	private final TournamentEventDetail startEvent;
	private final TournamentEventDetail endEvent;

	public TournamentCareerSpanRecordDetail(
		@JsonProperty("span") String span,
		@JsonProperty("start_date") Date startDate,
		@JsonProperty("start_tournament_event_id") int startTournamentEventId,
		@JsonProperty("start_tournament") String startTournament,
		@JsonProperty("start_level") String startLevel,
		@JsonProperty("end_date") Date endDate,
		@JsonProperty("end_tournament_event_id") int endTournamentEventId,
		@JsonProperty("end_tournament") String endTournament,
		@JsonProperty("end_level") String endLevel
	) {
		super(span, startDate, endDate);
		startEvent = new TournamentEventDetail(startTournamentEventId, startTournament, startLevel);
		endEvent = new TournamentEventDetail(endTournamentEventId, endTournament, endLevel);
	}

	public TournamentEventDetail getStartEvent() {
		return startEvent;
	}

	public TournamentEventDetail getEndEvent() {
		return endEvent;
	}

	@Override public String toString() {
		return format("%1$s (%2$td-%2$tm-%2$tY %3$s - %4$td-%4$tm-%4$tY %5$s)", getSpan(), getStartDate(), startEvent.getName(), getEndDate(), endEvent.getName());
	}
}
