package org.strangeforest.tcb.stats.model.records;

import java.util.*;

public class Record {

	private final String id;
	private final String name;
	private final String sql;
	private final String columns;
	private final String rankOrder;
	private final String displayOrder;
	private final RecordRowFactory rowFactory;
	private final List<RecordColumn> columnInfos;
	private boolean infamous;

	public Record(String id, String name, String sql, String columns, String rankOrder, String displayOrder, RecordRowFactory rowFactory, List<RecordColumn> columnInfos) {
		this.id = id;
		this.name = name;
		this.sql = sql;
		this.columns = columns;
		this.rankOrder = rankOrder;
		this.displayOrder = displayOrder;
		this.rowFactory = rowFactory;
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

	public RecordRowFactory getRowFactory() {
		return rowFactory;
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
