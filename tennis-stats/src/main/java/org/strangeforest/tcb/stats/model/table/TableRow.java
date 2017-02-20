package org.strangeforest.tcb.stats.model.table;

import java.util.*;
import java.util.stream.*;

public class TableRow {

	private final List<TableCell> c = new ArrayList<>();

	public TableRow(String value) {
		c.add(new TableCell(value));
	}

	public TableRow(String... values) {
		Stream.of(values).forEach(value -> c.add(new TableCell(value)));
	}

	public List<TableCell> getC() {
		return c;
	}

	public void addCell(String value) {
		c.add(new TableCell(value));
	}


	// Object methods

	@Override public String toString() {
		return String.valueOf(c);
	}
}
