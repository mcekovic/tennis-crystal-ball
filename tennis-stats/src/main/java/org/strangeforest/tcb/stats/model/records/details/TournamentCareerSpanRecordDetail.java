package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

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
}
