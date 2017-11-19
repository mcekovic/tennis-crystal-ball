package org.strangeforest.tcb.stats.model.records.details;

import org.strangeforest.tcb.stats.model.*;

import com.fasterxml.jackson.annotation.*;

public class H2HIntegerRecordDetail extends SimpleRecordDetail<Integer> {

	private final PlayerRow player2;

	public H2HIntegerRecordDetail(
		@JsonProperty("value") int value,
		@JsonProperty("player_id2") int playerId2,
		@JsonProperty("name2") String name2,
		@JsonProperty("country_id2") String countryId2,
		@JsonProperty("active2") Boolean active2
	) {
		super(value);
		player2 = new PlayerRow(2, playerId2, name2, countryId2, active2);
	}

	public PlayerRow getPlayer2() {
		return player2;
	}

	@Override public String toDetailString() {
		return "vs " + getPlayer2().shortName();
	}
}
