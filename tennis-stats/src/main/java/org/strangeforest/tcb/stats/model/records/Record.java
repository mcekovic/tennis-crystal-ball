package org.strangeforest.tcb.stats.model.records;

import java.util.*;

public class Record {

	private final String id;
	private final String name;
	private final String sql;
	private final String columns;
	private final String rankOrder;
	private final String displayOrder;
	private final RecordDetailFactory detailFactory;
	private final List<RecordColumn> columnInfos;
	private boolean infamous;

	public Record(String id, String name, String sql, String columns, String rankOrder, String displayOrder, Class<? extends RecordDetail> detailClass, List<RecordColumn> columnInfos) {
		this.id = id;
		this.name = name;
		this.sql = sql;
		this.columns = columns;
		this.rankOrder = rankOrder;
		this.displayOrder = displayOrder;
		this.detailFactory = RecordDetailFactory.forClass(detailClass);
		this.columnInfos = columnInfos;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSql() {
		return sql;
	}

	public String getColumns() {
		return columns;
	}

	public String getRankOrder() {
		return rankOrder;
	}

	public String getDisplayOrder() {
		return displayOrder;
	}

	public RecordDetailFactory getDetailFactory() {
		return detailFactory;
	}

	public List<RecordColumn> getColumnInfos() {
		return columnInfos;
	}

	public boolean isInfamous() {
		return infamous;
	}

	void setInfamous(boolean infamous) {
		this.infamous = infamous;
	}
}
