package org.strangeforest.tcb.stats.model.records.details;

import java.time.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.datatype.jsr310.deser.*;

public class DateRankingDiffRecordDetail extends BaseDateRankingDiffRecordDetail<Integer> {

	public DateRankingDiffRecordDetail(
		@JsonProperty("value") int value,
		@JsonProperty("player_id2") int playerId2,
		@JsonProperty("name2") String name2,
		@JsonProperty("country_id2") String countryId2,
		@JsonProperty("active2") Boolean active2,
		@JsonProperty("value1") int value1,
		@JsonProperty("value2") int value2,
		@JsonProperty("date") @JsonDeserialize(using = LocalDateDeserializer.class) LocalDate date
	) {
		super(value, playerId2, name2, countryId2, active2, value1, value2, date);
	}
}
