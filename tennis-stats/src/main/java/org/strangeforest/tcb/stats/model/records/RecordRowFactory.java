package org.strangeforest.tcb.stats.model.records;

public enum RecordRowFactory {

	INTEGER {
		@Override public RecordRow createRow(int rank, int playerId, String name, String countryId, boolean active) {
			return new IntegerRecordRow(rank, playerId, name, countryId, active);
		}
	};

	public abstract RecordRow createRow(int rank, int playerId, String name, String countryId, boolean active);
}
