package org.strangeforest.tcb.stats.model;

import java.util.*;
import java.util.stream.*;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;

public class DataTable {

	private final List<ColumnDescription> cols;
	private final List<TableRow> rows;

	public DataTable() {
		this.cols = new ArrayList<>();
		this.rows = new ArrayList<>();
	}

	public List<ColumnDescription> getCols() {
		return cols;
	}

	public List<TableRow> getRows() {
		return rows;
	}

	public void addColumn(String type, String label) {
		cols.add(new ColumnDescription(type, label));
	}

	public TableRow addRow(String... values) {
		TableRow row = new TableRow(asList(values).stream().map(TableCell::new).collect(toList()));
		rows.add(row);
		return row;
	}
}
