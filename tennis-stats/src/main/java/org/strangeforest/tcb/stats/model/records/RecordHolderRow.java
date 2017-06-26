package org.strangeforest.tcb.stats.model.records;

import java.util.function.*;

import org.strangeforest.tcb.stats.model.*;

public class RecordHolderRow<D extends RecordDetail> extends PlayerRow {

	private final D detail;
	private final BiFunction<Integer, D, String> detailUrlFormatter;

	public RecordHolderRow(int playerId, String name, String countryId, Boolean active, D detail, BiFunction<Integer, D, String> detailUrlFormatter) {
		super(1, playerId, name, countryId, active);
		this.detail = detail;
		this.detailUrlFormatter = detailUrlFormatter;
	}

	public D detail() {
		return detail;
	}

	public String getDetail() {
		return detail.toDetailString();
	}

	public String getDetailUrl() {
		return detailUrlFormatter != null ? detailUrlFormatter.apply(getPlayerId(), detail) : null;
	}
}
