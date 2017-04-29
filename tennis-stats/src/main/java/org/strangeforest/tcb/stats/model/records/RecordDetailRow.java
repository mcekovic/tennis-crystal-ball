package org.strangeforest.tcb.stats.model.records;

import java.util.function.*;

import org.strangeforest.tcb.stats.model.*;

import com.fasterxml.jackson.annotation.*;

public class RecordDetailRow<D extends RecordDetail> extends PlayerRow {

	private final D detail;
	private final BiFunction<Integer, D, String> detailUrlFormatter;

	public RecordDetailRow(int rank, int playerId, String name, String countryId, Boolean active, D detail, BiFunction<Integer, D, String> detailUrlFormatter) {
		super(rank, playerId, name, countryId, active);
		this.detail = detail;
		this.detailUrlFormatter = detailUrlFormatter;
	}

	@JsonUnwrapped
	public RecordDetail getDetail() {
		return detail;
	}

	public String getDetailUrl() {
		return detailUrlFormatter != null ? detailUrlFormatter.apply(getPlayerId(), detail) : null;
	}
}
