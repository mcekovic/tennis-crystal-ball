package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.Arrays.*;

public class TableRow {

	private final List<TableCell> c;

	public TableRow(List<TableCell> cells) {
		c = cells;
	}

	public TableRow(TableCell... cells) {
		c = new ArrayList<>();
		c.addAll(asList(cells));
	}

	public List<TableCell> getC() {
		return c;
	}

	public void addCell(String value) {
		c.add(new TableCell(value));
	}
}
