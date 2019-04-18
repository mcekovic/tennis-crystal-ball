package org.strangeforest.tcb.stats.model.records;

import java.util.*;
import java.util.function.*;

import static com.google.common.base.Strings.*;
import static org.strangeforest.tcb.stats.model.records.RecordCategory.*;

public class Record<D extends RecordDetail> {

	private final String id;
	private final String name;
	private final String sql;
	private final String columns;
	private final String rankOrder;
	private final String displayOrder;
	private final RecordDetailFactory detailFactory;
	private final BiFunction<Integer, D, String> detailURLFormatter;
	private final List<RecordColumn> columnInfos;
	private final String notes;
	private RecordCategory category;
	private boolean infamous;

	public Record(String id, String name, String sql, String columns, String rankOrder, String displayOrder, Class<D> detailClass, BiFunction<Integer, D, String> detailURLFormatter, List<RecordColumn> columnInfos) {
		this(id, name, sql, columns, rankOrder, displayOrder, detailClass, detailURLFormatter, columnInfos, N_A);
	}

	public Record(String id, String name, String sql, String columns, String rankOrder, String displayOrder, Class<D> detailClass, BiFunction<Integer, D, String> detailURLFormatter, List<RecordColumn> columnInfos, String notes) {
		this.id = id;
		this.name = name;
		this.sql = sql;
		this.columns = columns;
		this.rankOrder = rankOrder;
		this.displayOrder = displayOrder;
		this.detailFactory = new RecordDetailFactory(detailClass);
		this.detailURLFormatter = detailURLFormatter;
		this.columnInfos = columnInfos;
		this.notes = !isNullOrEmpty(notes) ? notes : null;
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

	public BiFunction<Integer, D, String> getDetailURLFormatter() {
		return detailURLFormatter;
	}

	public List<RecordColumn> getColumnInfos() {
		return columnInfos;
	}

	public String getNotes() {
		return notes;
	}

	public RecordCategory getCategory() {
		return category;
	}

	void setCategory(RecordCategory category) {
		this.category = category;
	}

	public boolean isInfamous() {
		return infamous;
	}

	void setInfamous(boolean infamous) {
		this.infamous = infamous;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Record)) return false;
		Record<?> record = (Record<?>)o;
		return id.equals(record.id);
	}

	@Override public int hashCode() {
		return id.hashCode();
	}

	@Override public String toString() {
		return name;
	}
}
