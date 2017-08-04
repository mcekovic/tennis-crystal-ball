package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class SeasonRankingDiffRecordDetail extends BaseSeasonRankingDiffRecordDetail<Integer> {

	public SeasonRankingDiffRecordDetail(
		@JsonProperty("value") int value,
		@JsonProperty("player_id2") int playerId2,
		@JsonProperty("name2") String name2,
		@JsonProperty("country_id2") String countryId2,
		@JsonProperty("active2") Boolean active2,
		@JsonProperty("value1") int value1,
		@JsonProperty("value2") int value2,
		@JsonProperty("season") int season
	) {
		super(value, playerId2, name2, countryId2, active2, value1, value2, season);
	}
}
