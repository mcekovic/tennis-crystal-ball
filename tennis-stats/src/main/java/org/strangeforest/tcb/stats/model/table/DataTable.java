package org.strangeforest.tcb.stats.model.table;

import java.util.*;

public class DataTable {

	private final List<ColumnDescription> cols = new ArrayList<>();
	private final List<TableRow> rows = new ArrayList<>();

	public List<ColumnDescription> getCols() {
		return cols;
	}

	public List<TableRow> getRows() {
		return rows;
	}

	public void addColumn(String type, String label) {
		cols.add(new ColumnDescription(type, label));
	}

	public TableRow addRow(String value) {
		TableRow row = new TableRow(value);
		rows.add(row);
		return row;
	}

	public TableRow addRow(String... values) {
		TableRow row = new TableRow(values);
		rows.add(row);
		return row;
	}
}
