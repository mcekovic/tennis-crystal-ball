package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class H2HWinningPctRecordDetail extends H2HWonLostRecordDetail {

	public H2HWinningPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("lost") int lost,
		@JsonProperty("player_id2") int playerId2,
		@JsonProperty("name2") String name2,
		@JsonProperty("country_id2") String countryId2,
		@JsonProperty("active2") Boolean active2
	) {
		super(won, lost, playerId2, name2, countryId2, active2);
	}

	@Override public String getValue() {
		return wonLost.getWonPctStr(2);
	}
}
