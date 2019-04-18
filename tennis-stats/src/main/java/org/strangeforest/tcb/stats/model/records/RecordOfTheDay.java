package org.strangeforest.tcb.stats.model.records;

public class RecordOfTheDay<D extends RecordDetail> {

	private final Record<D> record;
	private final RecordDetailRow<D> recordDetail;

	public RecordOfTheDay(Record<D> record, RecordDetailRow<D> recordDetail) {
		this.record = record;
		this.recordDetail = recordDetail;
	}

	public Record<D> getRecord() {
		return record;
	}

	public RecordDetailRow<D> getRecordDetail() {
		return recordDetail;
	}
}
